package com.github.carloseduardofsousa.listadetarefasprofissional

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListarTarefasActivity : AppCompatActivity() {

    lateinit var rvTarefas: RecyclerView
    lateinit var btnIrCriar: Button
    lateinit var helper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.listar_tarefa_activity)

        helper = SQLiteHelper(this)
        rvTarefas = findViewById(R.id.rvTarefas)
        btnIrCriar = findViewById(R.id.btnIrCriar)

        // Configura o visual da lista
        rvTarefas.layoutManager = LinearLayoutManager(this)

        // Botão que pula para a tela de cadastro(sua MainActivity)
        btnIrCriar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // O segredo profissional: recarregar a lista sempre que voltar para esta tela
    override fun onResume() {
        super.onResume()
        atualizarLista()
    }

    fun atualizarLista() {
        val listaDoBanco = helper.listarTarefas()
        val adapter = TarefaAdapter(listaDoBanco)
        rvTarefas.adapter = adapter
    }
}