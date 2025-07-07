package app.netlify.dev4rju9.easyapplymailer.utils

object Utility {

    fun extractPlaceholdersCount(body: String): Int {
        val regex = "\\[text]".toRegex()
        return regex.findAll(body).count()
    }

    fun replacePlaceholders(body: String, replacements: List<String>): String {
        var result = body
        val regex = "\\[text]".toRegex()
        val iterator = replacements.iterator()

        result = regex.replace(result) {
            if (iterator.hasNext()) iterator.next() else "[text]"
        }
        return result
    }

}