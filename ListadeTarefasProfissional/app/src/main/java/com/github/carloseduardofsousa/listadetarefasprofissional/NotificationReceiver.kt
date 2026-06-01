package com.github.carloseduardofsousa.listadetarefasprofissional

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nomeTarefa = intent.getStringExtra("NOME_TAREFA") ?: "Tarefa"
        val canalId = "notificacao_tarefa"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Criar o canal de notificação (obrigatório para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(canalId, "Lembretes de Tarefas", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(canal)
        }

        val notificacao = NotificationCompat.Builder(context, canalId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sua tarefa é amanhã!")
            .setContentText("Não esqueça de: $nomeTarefa")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notificacao)
    }
}