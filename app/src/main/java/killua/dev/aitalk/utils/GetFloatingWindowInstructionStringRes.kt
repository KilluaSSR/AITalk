package killua.dev.aitalk.utils

import androidx.annotation.StringRes
import killua.dev.aitalk.R
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode

@StringRes
fun getFloatingWindowInstructionStringRes(questionMode: FloatingWindowQuestionMode, model: AIModel): Int {
    return when (questionMode) {
        FloatingWindowQuestionMode.isThatTrueYNQuestion -> when (model) {
            AIModel.ChatGPT -> R.string.fw_instruction_is_that_true_yn_question_chatgpt
            AIModel.Claude -> R.string.fw_instruction_is_that_true_yn_question_claude
            AIModel.Gemini -> R.string.fw_instruction_is_that_true_yn_question_gemini
            AIModel.DeepSeek -> R.string.fw_instruction_is_that_true_yn_question_deepseek
            AIModel.Grok -> R.string.fw_instruction_is_that_true_yn_question_grok
        }
        FloatingWindowQuestionMode.isThatTrueWithExplain -> when (model) {
            AIModel.ChatGPT -> R.string.fw_instruction_is_that_true_with_explain_chatgpt
            AIModel.Claude -> R.string.fw_instruction_is_that_true_with_explain_claude
            AIModel.Gemini -> R.string.fw_instruction_is_that_true_with_explain_gemini
            AIModel.DeepSeek -> R.string.fw_instruction_is_that_true_with_explain_deepseek
            AIModel.Grok -> R.string.fw_instruction_is_that_true_with_explain_grok
        }
        FloatingWindowQuestionMode.explainBriefly -> when (model) {
            AIModel.ChatGPT -> R.string.fw_instruction_explain_briefly_chatgpt
            AIModel.Claude -> R.string.fw_instruction_explain_briefly_claude
            AIModel.Gemini -> R.string.fw_instruction_explain_briefly_gemini
            AIModel.DeepSeek -> R.string.fw_instruction_explain_briefly_deepseek
            AIModel.Grok -> R.string.fw_instruction_explain_briefly_grok
        }
        FloatingWindowQuestionMode.explainVerbose -> when (model) {
            AIModel.ChatGPT -> R.string.fw_instruction_explain_verbose_chatgpt
            AIModel.Claude -> R.string.fw_instruction_explain_verbose_claude
            AIModel.Gemini -> R.string.fw_instruction_explain_verbose_gemini
            AIModel.DeepSeek -> R.string.fw_instruction_explain_verbose_deepseek
            AIModel.Grok -> R.string.fw_instruction_explain_verbose_grok
        }
        FloatingWindowQuestionMode.translate -> when (model) {
            AIModel.ChatGPT -> R.string.fw_instruction_translate_chatgpt
            AIModel.Claude -> R.string.fw_instruction_translate_claude
            AIModel.Gemini -> R.string.fw_instruction_translate_gemini
            AIModel.DeepSeek -> R.string.fw_instruction_translate_deepseek
            AIModel.Grok -> R.string.fw_instruction_translate_grok
        }
    }
}