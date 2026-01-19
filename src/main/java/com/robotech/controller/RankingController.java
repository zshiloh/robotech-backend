package com.robotech.controller;

import com.robotech.dto.RankingClubResponse;
import com.robotech.dto.RankingResponse;
import com.robotech.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/ranking")
public class RankingController {
    
    @Autowired
    private RankingService rankingService;
    
    /**
     * GET /api/public/ranking/individual?categoriaId={id}
     * Obtener ranking individual de una categoría
     */
    @GetMapping("/individual")
    public ResponseEntity<List<RankingResponse>> obtenerRankingIndividual(
            @RequestParam Integer categoriaId) {
        
        List<RankingResponse> ranking = rankingService.obtenerRankingIndividual(categoriaId, null);
        return ResponseEntity.ok(ranking);
    }
    
    /**
     * GET /api/public/ranking/club?categoriaId={id}
     * Obtener ranking de clubes de una categoría
     */
    @GetMapping("/club")
    public ResponseEntity<List<RankingClubResponse>> obtenerRankingClub(
            @RequestParam Integer categoriaId) {
        
        List<RankingClubResponse> ranking = rankingService.obtenerRankingClubes(categoriaId, null);
        return ResponseEntity.ok(ranking);
    }
}