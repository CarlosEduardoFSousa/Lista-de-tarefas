data class Tarefa(
    val id: Int = 0,
    val nomeT: String,
    val dataEntrega: String,
    val assunto: String,
    val status: Int = 0,
    val prioridade: Int = 1,
    val sincronizado: Int = 0
)