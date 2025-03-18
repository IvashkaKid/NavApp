package com.example.navapp.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.navapp.R
import com.example.navapp.data.models.LoginRequest
import com.example.navapp.data.connection.AuthApiService
import com.example.navapp.data.connection.RetrofitClient
import com.example.navapp.data.storage.SecureStorage
import com.example.navapp.data.storage.TokenManager
import com.example.navapp.databinding.FragmentLoginBinding
import com.example.navapp.MainActivity
import com.google.gson.JsonParser
import java.io.IOException
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var authService: AuthApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        authService = RetrofitClient.getAuthService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val login = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!isValidLogin(login)) {
                binding.etUsername.error = "Логин может содержать только буквы и цифры"
                return@setOnClickListener
            }

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = authService.login(LoginRequest(login, password))
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        if (token != null) {
                            TokenManager.saveToken(requireContext(), token)
                            TokenManager.saveLogin(requireContext(), login)

                            val apiService = RetrofitClient.getApiService(requireContext())
                            try {
                                val userResponse = apiService.getUser()
                                if (userResponse.isSuccessful) {
                                    startActivity(Intent(requireContext(), MainActivity::class.java))
                                    requireActivity().finish()
                                } else {
                                    val errorBody = userResponse.errorBody()?.string()
                                    val errorMessage = try {
                                        JsonParser.parseString(errorBody)
                                            .asJsonObject["message"].asString
                                    } catch (e: Exception) {
                                        "Ошибка: ${userResponse.code()}"
                                    }
                                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    "Ошибка загрузки пользователя: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Invalid response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            JsonParser.parseString(errorBody).asJsonObject["message"].asString
                        } catch (e: Exception) {
                            "Ошибка: ${response.code()}"
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidLogin(login: String): Boolean {
        val regex = Regex("^[a-zA-Z0-9]+\$")
        return login.matches(regex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}