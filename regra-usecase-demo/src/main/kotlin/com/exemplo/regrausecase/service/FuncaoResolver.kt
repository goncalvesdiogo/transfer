package com.exemplo.regrausecase.service

import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Tipo de uma funcao registrada: recebe os argumentos (como String, ja separados
 * por virgula e sem espacos nas pontas) e devolve o valor resolvido.
 */
typealias FuncaoCondicao = (List<String>) -> Any

/**
 * Resolve expressoes de funcao usadas no campo "valor" das condicoes, no formato:
 *   $nomeDaFuncao
 *   $nomeDaFuncao(arg1, arg2, ...)
 *
 * Nao usa regex: a sintaxe e simples o suficiente pra ser parseada com
 * indexOf/substring, o que deixa o parsing mais explicito e facil de debugar.
 *
 * Duas formas de registrar uma funcao:
 *
 * 1) Lambda inline (`registrarFuncao`) - para logica pequena, criada so pra isso.
 * 2) Referencia de funcao ja existente no codigo (`registrarFuncaoExistente`) -
 *    reaproveita uma funcao de dominio que voce ja tem (ex: calculo de dia util,
 *    regra de vigencia, etc). Os argumentos String da condicao sao convertidos
 *    automaticamente para os tipos reais dos parametros via reflection, inclusive
 *    respeitando parametros com valor default (nao precisa passar todos).
 */
@Component
class FuncaoResolver(private val clock: Clock) {

    private val funcoes: MutableMap<String, FuncaoCondicao> = mutableMapOf()

    init {
        // $hoje            -> data de hoje
        // $hoje(3)         -> hoje + 3 dias
        // $hoje(-1)        -> hoje - 1 dia
        registrarFuncao("hoje") { args ->
            val dias = args.firstOrNull()?.toLongOrNull() ?: 0L
            LocalDate.now(clock).plusDays(dias)
        }

        registrarFuncao("getHoje"){
            LocalDate.now(clock)
        }
    }

    /**
     * Registra uma funcao a partir de uma lambda inline.
     */
    fun registrarFuncao(nome: String, funcao: FuncaoCondicao) {
        funcoes[nome] = funcao
    }

    /**
     * Registra uma funcao a partir de uma referencia ja existente no codigo
     * (ex: `::minhaFuncaoDeDominio` ou `objeto::metodoDeInstancia`).
     *
     * Os argumentos vindos da condicao (sempre String) sao convertidos para o
     * tipo real de cada parametro da funcao (Long, Int, Double, BigDecimal,
     * LocalDate, Boolean, String). Parametros nao informados na condicao usam
     * o valor default declarado na funcao, se houver.
     *
     * Exemplo:
     *   fun proximoDiaUtil(dataBase: LocalDate, feriadosBrasil: Boolean = true): LocalDate { ... }
     *   funcaoResolver.registrarFuncaoExistente("proximoDiaUtil", ::proximoDiaUtil)
     *
     *   condicao: {"campo":"dt_vcto", "operacao":"==", "valor":"$proximoDiaUtil(2026-07-10)"}
     */
    fun registrarFuncaoExistente(nome: String, funcao: KFunction<*>) {
        funcoes[nome] = { args ->
            val parametros = funcao.parameters
            require(args.size <= parametros.size) {
                "Funcao '$nome' aceita no maximo ${parametros.size} argumento(s), recebeu ${args.size}"
            }

            val argumentosNomeados = mutableMapOf<KParameter, Any?>()
            parametros.forEachIndexed { index, parametro ->
                if (index < args.size) {
                    argumentosNomeados[parametro] = converterArgumento(nome, args[index], parametro)
                }
                // parametros nao informados ficam de fora do map: se tiverem
                // default, o Kotlin usa o default; se forem obrigatorios,
                // callBy lanca IllegalArgumentException com mensagem clara.
            }

            funcao.callBy(argumentosNomeados)
                ?: throw IllegalStateException("Funcao '$nome' retornou null, o que nao pode ser usado como valor de comparacao")
        }
    }

    /**
     * Resolve o valor da condicao. Se nao comecar com "$", devolve o valor
     * literal (string) sem nenhuma interpretacao especial.
     */
    fun resolver(valor: String): Any {
        val expressao = valor.trim()
        if (!expressao.startsWith("$")) {
            return expressao
        }

        val (nomeFuncao, args) = parsearChamada(expressao)

        val funcao = funcoes[nomeFuncao]
            ?: throw IllegalArgumentException(
                "Funcao '$nomeFuncao' nao registrada (expressao: '$valor'). " +
                    "Funcoes disponiveis: ${funcoes.keys.joinToString(", ")}"
            )

        return funcao(args)
    }

    private fun parsearChamada(expressao: String): Pair<String, List<String>> {
        val semCifrao = expressao.removePrefix("$")
        val abreParenteses = semCifrao.indexOf('(')

        if (abreParenteses == -1) {
            return semCifrao to emptyList()
        }

        require(semCifrao.endsWith(")")) {
            "Expressao de funcao mal formada, faltou fechar parenteses: '$expressao'"
        }

        val nomeFuncao = semCifrao.substring(0, abreParenteses)
        val argsBrutos = semCifrao.substring(abreParenteses + 1, semCifrao.length - 1)
        val args = if (argsBrutos.isBlank()) {
            emptyList()
        } else {
            argsBrutos.split(",").map { it.trim() }
        }

        return nomeFuncao to args
    }

    private fun converterArgumento(nomeFuncao: String, valor: String, parametro: KParameter): Any? {
        val tipo: KType = parametro.type

        if (valor.equals("null", ignoreCase = true)) {
            require(tipo.isMarkedNullable) {
                "Funcao '$nomeFuncao': parametro '${parametro.name}' nao aceita null"
            }
            return null
        }

        return try {
            when (tipo.classifier) {
                Long::class -> valor.toLong()
                Int::class -> valor.toInt()
                Double::class -> valor.toDouble()
                Boolean::class -> valor.toBooleanStrict()
                String::class -> valor
                BigDecimal::class -> BigDecimal(valor)
                LocalDate::class -> LocalDate.parse(valor)
                else -> throw IllegalArgumentException(
                    "Funcao '$nomeFuncao': tipo de parametro '${tipo}' (parametro '${parametro.name}') " +
                        "nao tem conversao automatica suportada. Tipos suportados: Long, Int, Double, " +
                        "Boolean, String, BigDecimal, LocalDate."
                )
            }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException(
                "Funcao '$nomeFuncao': argumento '$valor' invalido para o parametro " +
                    "'${parametro.name}' (esperado ${tipo}): ${e.message}"
            )
        }
    }
}

