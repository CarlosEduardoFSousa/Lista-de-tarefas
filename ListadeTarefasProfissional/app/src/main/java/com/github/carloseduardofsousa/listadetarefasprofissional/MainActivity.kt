package com.github.carloseduardofsousa.listadetarefasprofissional

import Tarefa
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    lateinit var edtNome: EditText
    lateinit var edtDate: EditText
    lateinit var edtAssunto: EditText
    lateinit var btnCriar: Button
    lateinit var helper: SQLiteHelper
    private var idTarefaEdicao: Int = -1

    @SuppressLint("DefaultLocale")
    private fun mostrarCalendario() {
        val calendario = java.util.Calendar.getInstance()
        val ano = calendario.get(java.util.Calendar.YEAR)
        val mes = calendario.get(java.util.Calendar.MONTH)
        val dia = calendario.get(java.util.Calendar.DAY_OF_MONTH)

        val dpd = android.app.DatePickerDialog(this, { _, anoSel, mesSel, diaSel ->
            // O mês começa em 0, então somamos 1.
            // O format garante que fique 01/05/2026 e não 1/5/2026
            val dataFormatada = String.format("%02d/%02d/%d", diaSel, mesSel + 1, anoSel)
            edtDate.setText(dataFormatada)
        }, ano, mes, dia)

        // Define a data mínima como "agora" (o tempo atual em milissegundos)
        dpd.datePicker.minDate = System.currentTimeMillis()

        dpd.show()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun agendarNotificacao(nomeTarefa: String, dataTarefa: String) {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

        try {
            val date = sdf.parse(dataTarefa)
            if (date != null) {
                val calendario = java.util.Calendar.getInstance()
                calendario.time = date
                calendario.add(java.util.Calendar.DAY_OF_YEAR, -1)
                calendario.set(java.util.Calendar.HOUR_OF_DAY, 9)
                calendario.set(java.util.Calendar.MINUTE, 0)

                // Só agenda se a data do alarme ainda não passou
                if (calendario.timeInMillis > System.currentTimeMillis()) {
                    val intent = Intent(this, NotificationReceiver::class.java)
                    intent.putExtra("NOME_TAREFA", nomeTarefa)

                    val pendingIntent = android.app.PendingIntent.getBroadcast(
                        this,
                        // Use um ID único baseado no tempo para não sobrescrever alarmes de outras tarefas
                        System.currentTimeMillis().toInt(),
                        intent,
                        android.app.PendingIntent.FLAG_IMMUTABLE
                    )

                    // Verifique se o import está: import android.app.AlarmManager
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        // Para Android 12 ou superior
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendario.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            // Se não tiver permissão de alarme exato, usa o normal
                            alarmManager.set(AlarmManager.RTC_WAKEUP, calendario.timeInMillis, pendingIntent)
                        }
                    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        // Para Android 6 até 11
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendario.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        // Versões bem antigas
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendario.timeInMillis, pendingIntent)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissaoNotificacao = android.Manifest.permission.POST_NOTIFICATIONS

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, permissaoNotificacao) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,arrayOf(permissaoNotificacao), 101)
            }
        }

        // Faz a seta de saida aparecer no topo
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Muda o título da tela
        supportActionBar?.title = "Voltar"

        edtNome = findViewById(R.id.txtNomeT)
        edtDate = findViewById(R.id.txtData)
        edtAssunto = findViewById(R.id.txtAssunto)
        btnCriar = findViewById(R.id.btnCriar)
        helper = SQLiteHelper(this)

        idTarefaEdicao = intent.getIntExtra("TAREFA_ID", -1)

        if(idTarefaEdicao != -1){
            // MODO EDIÇÃO: Busca os dados e preenche os campos
            val tarefa = helper.buscarTarefaPorId(idTarefaEdicao)
            tarefa?.let{
                edtNome.setText(it.nomeT)
                edtDate.setText(it.dataEntrega)
                edtAssunto.setText(it.assunto)
                btnCriar.text = "Atualizar Tarefa" // Muda o texto do botão
            }
        }

        // Impede que o teclado abra quando clicar no campo
        edtDate.isFocusable = false
        edtDate.isClickable = true

        edtDate.setOnClickListener {
            mostrarCalendario()
        }

        btnCriar.setOnClickListener {
            val nome = edtNome.text.toString()
            val data = edtDate.text.toString()
            val assunto = edtAssunto.text.toString()

            // 1. Validação (deve vir antes de salvar qualquer coisa)
            if (nome.isEmpty()) {
                edtNome.error = "Digite o nome"
                return@setOnClickListener
            }

            // 2. Agendar a notificação AGORA (com o nome garantido)
            agendarNotificacao(nome,data)

            // 3. Lógica de Salvar ou Atualizar
            if (idTarefaEdicao == -1) {
                // MODO CRIAR: Salva uma nova tarefa
                val novaTarefa = Tarefa(nomeT = nome, dataEntrega = data, assunto = assunto)
                val resultado = helper.inserirTarefa(novaTarefa)

                if (resultado != -1L) {
                    Toast.makeText(this, "Tarefa salva!", Toast.LENGTH_SHORT).show()
                    finish() // Fecha e volta para a lista
                } else {
                    Toast.makeText(this, "Erro ao salvar!", Toast.LENGTH_SHORT).show()
                }

            } else {
                // MODO ATUALIZAR: Edita a tarefa que já existe
                val tarefaEditada = Tarefa(id = idTarefaEdicao, nomeT = nome, dataEntrega = data, assunto = assunto)
                val resultado = helper.atualizarTarefa(tarefaEditada)

                if (resultado > 0) {
                    Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
                    finish() // Fecha e volta para a lista
                } else {
                    Toast.makeText(this, "Erro ao atualizar!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}