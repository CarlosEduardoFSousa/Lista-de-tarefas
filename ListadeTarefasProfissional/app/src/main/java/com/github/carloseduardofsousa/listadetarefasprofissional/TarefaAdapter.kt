package com.github.carloseduardofsousa.listadetarefasprofissional

import Tarefa
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TarefaAdapter(private val lista: List<Tarefa>) :
    RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder>() {

    class TarefaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeItem)
        val txtData: TextView = view.findViewById(R.id.txtDataItem)
        val btnExcluir: ImageButton = view.findViewById(R.id.btnExcluirItem) // Adicionado aqui
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarefaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarefa, parent, false)
        return TarefaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarefaViewHolder, position: Int) {
        val tarefa = lista[position]

        // 1. Preenche os textos
        holder.txtNome.text = tarefa.nomeT
        holder.txtData.text = tarefa.dataEntrega

        // 2. Clique no ícone de lixeira (Excluir)
        holder.btnExcluir.setOnClickListener {
            val context = holder.itemView.context

            android.app.AlertDialog.Builder(context)
                .setTitle("Excluir Tarefa")
                .setMessage("Tem certeza que deseja excluir '${tarefa.nomeT}'?")
                .setPositiveButton("Sim") { _, _ ->
                    val helper = SQLiteHelper(context)
                    helper.excluirTarefa(tarefa.id)
                    // Faz um "cast" para avisar a Activity que a lista mudou
                    (context as ListarTarefasActivity).atualizarLista()
                }
                .setNegativeButton("Não", null)
                .show()
        }

        // 3. Clique no corpo da tarefa (Para abrir Detalhes/Editar)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetalhesTarefaActivity::class.java)
            intent.putExtra("TAREFA_ID", tarefa.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = lista.size
}