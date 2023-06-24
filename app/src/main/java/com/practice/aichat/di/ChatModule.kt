package com.practice.AIChat.di

import com.practice.AIChat.chat.data.ConversationRepository
import com.practice.AIChat.chat.data.api.OpenAIRepository
import com.practice.AIChat.chat.domain.usecase.ObserveMessagesUseCase
import com.practice.AIChat.chat.domain.usecase.ResendMessageUseCase
import com.practice.AIChat.chat.domain.usecase.SendChatRequestUseCase
import com.practice.AIChat.chat.ui.ChatViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

val chatModule = module {
    viewModel {
        ChatViewModel(get(), get(), get())
    }
    single { OpenAIRepository(openAI = get()) }
    single { ConversationRepository() }

    single { SendChatRequestUseCase(openAIRepository = get(), conversationRepository = get()) }
    single { ResendMessageUseCase(openAIRepository = get(), conversationRepository = get()) }
    single { ObserveMessagesUseCase(conversationRepository = get()) }
}