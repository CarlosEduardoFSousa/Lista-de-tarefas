package com.github.carloseduardofsousa.listadetarefasprofissional

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetalhesTarefaActivity : AppCompatActivity() {

    private lateinit var helper: SQLiteHelper
    private var tarefaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detalhes_tarefa_activity)

        // Faz a seta de saida aparecer no topo
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //Muda o título da tela
        supportActionBar?.title = "Voltar"

        helper = SQLiteHelper(this)

        // Pega o ID que o Adapter mandou
        tarefaId = intent.getIntExtra("TAREFA_ID", -1)

        // Busca no banco e preenche a tela
        carregarDadosTarefa()

        // Botão de editar
        findViewById<View>(R.id.fabEditar).setOnClickListener {
            val intent = Intent(this, MainActivity :: class.java)
            intent.putExtra("TAREFA_ID", tarefaId)// Passa o ID para o MainActivity
            startActivity(intent)
            finish()
        }
    }

    private fun carregarDadosTarefa(){
        val tarefa = helper.buscarTarefaPorId(tarefaId)

        if (tarefa != null){
            findViewById<TextView>(R.id.txtNomeDetalhe).text = tarefa.nomeT
            findViewById<TextView>(R.id.txtDataDetalhe).text = tarefa.dataEntrega
            findViewById<TextView>(R.id.txtAssuntoDetalhe).text = tarefa.assunto
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}