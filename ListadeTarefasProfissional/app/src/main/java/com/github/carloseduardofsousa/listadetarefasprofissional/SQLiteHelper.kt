package com.github.carloseduardofsousa.listadetarefasprofissional

import Tarefa
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, "BdDTarefas.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val sql = "CREATE TABLE tarefas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " nomet TEXT NOT NULL," +
                " data_final TEXT," +
                " assunto TEXT," +
                " status INTEGER DEFAULT 0," +
                " prioridade INTEGER," +
                " sincronizado INTEGER DEFAULT 0" +
                ");"
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tarefas")
        onCreate(db)
    }

    fun atualizarTarefa(tarefa: Tarefa): Int{
        val db = this.writableDatabase
        val valores = android.content.ContentValues()
        valores.put("nomet", tarefa.nomeT)
        valores.put("data_final", tarefa.dataEntrega)
        valores.put("assunto", tarefa.assunto)

        // Atualiza onde o ID for igual ao da tarefa que foi passado
        val resultado = db.update("tarefas", valores, "id = ?", arrayOf(tarefa.id.toString()))
        db.close()
        return resultado
    }

    fun inserirTarefa(tarefa: Tarefa): Long {
        val db = this.writableDatabase
        val valores = ContentValues()
        valores.put("nomet", tarefa.nomeT)
        valores.put("data_final", tarefa.dataEntrega)
        valores.put("assunto", tarefa.assunto)
        valores.put("prioridade", tarefa.prioridade)
        valores.put("status", tarefa.status)
        valores.put("sincronizado", tarefa.sincronizado)

        val resultado = db.insert("tarefas", null, valores)
        db.close()
        return resultado
    }
    fun buscarTarefaPorId(id: Int): Tarefa? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tarefas WHERE id = ?", arrayOf(id.toString()))

        var tarefa: Tarefa? = null

        if (cursor.moveToFirst()) {
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nomet"))
            val data = cursor.getString(cursor.getColumnIndexOrThrow("data_final"))
            val assunto = cursor.getString(cursor.getColumnIndexOrThrow("assunto"))

            // Aqui criamos o objeto Tarefa com os dados do banco
            tarefa = Tarefa(
                id = id,
                nomeT = nome,
                dataEntrega = data,
                assunto = assunto
            )
        }
        cursor.close()
        db.close()
        return tarefa
    }

    fun listarTarefas(): List<Tarefa> {
        val lista = mutableListOf<Tarefa>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tarefas", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("nomet"))
                val data = cursor.getString(cursor.getColumnIndexOrThrow("data_final"))
                val assunto = cursor.getString(cursor.getColumnIndexOrThrow("assunto"))
                val status = cursor.getInt(cursor.getColumnIndexOrThrow("status"))
                val prioridade = cursor.getInt(cursor.getColumnIndexOrThrow("prioridade"))
                val sincronizado = cursor.getInt(cursor.getColumnIndexOrThrow("sincronizado"))

                lista.add(Tarefa(id, nome, data, assunto, status, prioridade, sincronizado))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun excluirTarefa(id: Int): Int{
        val db = this.writableDatabase
        // Delete retorna o número de linhas removidas. Se for > 0, deu certo
        val resultado = db.delete("tarefas","id = ?", arrayOf(id.toString()))
        db.close()
        return resultado
    }
}