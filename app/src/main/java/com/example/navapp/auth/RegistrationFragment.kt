package com.example.navapp.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.navapp.data.models.RegisterRequest
import com.example.navapp.data.connection.AuthApiService
import com.example.navapp.data.connection.NetworkUtils
import com.example.navapp.data.connection.RetrofitClient
import com.example.navapp.data.storage.SecureStorage
import com.example.navapp.data.storage.TokenManager
import com.example.navapp.databinding.FragmentRegistrationBinding
import com.example.navapp.MainActivity
import com.google.gson.JsonParser
import java.io.IOException
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private lateinit var authService: AuthApiService // Изменено на lateinit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        authService = RetrofitClient.getAuthService(requireContext()) // Инициализация здесь
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val login = binding.etLogin.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!validateInput(name, login, password)) return@setOnClickListener

            if (!NetworkUtils.isInternetAvailable(requireContext())) {
                showError("Нет подключения к интернету")
                return@setOnClickListener
            }

            if (login.isEmpty() || name.isEmpty() || password.isEmpty()) {
                showError("Все поля обязательны для заполнения")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showError("Пароль должен быть не менее 6 символов")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = authService.register(
                        RegisterRequest(
                            login = login,
                            name = name,
                            password = password
                        )
                    )

                    if (response.isSuccessful) {
                        response.body()?.token?.let { token ->
                            TokenManager.saveToken(requireContext(), token)
                            TokenManager.saveLogin(requireContext(), login)
                            navigateToMain()
                        } ?: showError("Пустой ответ от сервера")
                    } else {
                        val errorCode = response.code()
                        val errorMessage = try {
                            response.errorBody()?.string()?.let {
                                JsonParser.parseString(it)
                                    .asJsonObject["message"]
                                    .asString
                            } ?: "Ошибка $errorCode"
                        } catch (e: Exception) {
                            "Ошибка $errorCode"
                        }
                        showError(errorMessage)
                    }
                } catch (e: IOException) {
                    showError("Проверьте подключение к интернету")
                } catch (e: Exception) {
                    showError("Неизвестная ошибка: ${e.message}")
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun validateInput(name: String, login: String, password: String): Boolean {
        val nameRegex = Regex("^[a-zA-Zа-яА-Я0-9 ]+\$")
        val loginRegex = Regex("^[a-zA-Z0-9]+\$")

        if (!name.matches(nameRegex)) {
            binding.etName.error = "Только буквы, цифры и пробелы" // Исправлено etName
            return false
        }

        if (!login.matches(loginRegex)) {
            binding.etLogin.error = "Только латинские буквы и цифры" // Добавлена проверка логина
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Минимум 6 символов"
            return false
        }

        return true
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}