package com.krzyzowski.knowledgeboost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.krzyzowski.knowledgeboost.ui.KnowledgeBoostRoot
import com.krzyzowski.knowledgeboost.ui.KnowledgeBoostTheme

class MainActivity : ComponentActivity() {
    private val container by lazy { (application as KnowledgeBoostApp).container }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KnowledgeBoostTheme {
                KnowledgeBoostRoot(container.today, container.libraryStore)
            }
        }
    }
}
