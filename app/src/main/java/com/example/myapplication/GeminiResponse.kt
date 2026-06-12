package com.example.myapplication.data

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: GeminiContent
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)