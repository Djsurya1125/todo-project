package com.todoapp.modern

import android.app.Application
import com.todoapp.modern.di.AppContainer

/**
 * Main Application class for the Todo App
 * Uses manual dependency injection
 */
class TodoApplication : Application() {
    
    // Manual dependency injection container
    lateinit var appContainer: AppContainer
        private set
    
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
