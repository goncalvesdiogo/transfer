package com.example.recommendation

import com.example.recommendation.domain.RecommendationResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recommendations")
class RecommendationController(
    private val service: RecommendationService
) {
    @GetMapping("/{userId}")
    fun getRecommendations(@PathVariable userId: String): List<RecommendationResult> =
        service.fetchAndProcess(userId)
}
