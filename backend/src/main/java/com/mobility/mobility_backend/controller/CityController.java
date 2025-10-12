package com.mobility.mobility_backend.controller;
 
import com.mobility.mobility_backend.dto.CityDTO;
import com.mobility.mobility_backend.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cities")  // ✅ Tous les endpoints commencent par /api/cities
public class CityController {
	
	 @Autowired
	    private CityService cityService;
	 
	 
	 /**
	     * Crée une nouvelle ville
	     * POST /api/cities
	     */
	 @PostMapping
	    public ResponseEntity<CityDTO> createCity(@RequestBody CityDTO cityDTO) {
	        try {
	            CityDTO createdCity = cityService.createCity(cityDTO.getName());
	            return new ResponseEntity<>(createdCity, HttpStatus.CREATED);
	        } catch (RuntimeException e) {
	            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
	        }
	    }
	 
	 
	   /**
	     * Récupère toutes les villes
	     * GET /api/cities
	     */
	    @GetMapping
	    public ResponseEntity<List<CityDTO>> getAllCities() {
	        List<CityDTO> cities = cityService.getAllCities();
	        return new ResponseEntity<>(cities, HttpStatus.OK);
	    }
	    
	    
	    /**
	     * Récupère une ville par son ID
	     * GET /api/cities/{id}
	     */
	    @GetMapping("/{id}")
	    public ResponseEntity<CityDTO> getCityById(@PathVariable Long id) {
	        Optional<CityDTO> city = cityService.getCityById(id);
	        return city.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
	                  .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	    }
	    
	    /**
	     * Récupère une ville par son nom
	     * GET /api/cities/name/{name}
	     */
	    @GetMapping("/name/{name}")
	    public ResponseEntity<CityDTO> getCityByName(@PathVariable String name) {
	        Optional<CityDTO> city = cityService.getCityByName(name);
	        return city.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
	                  .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	    }
	    
	    /**
	     * Supprime une ville par son ID
	     * DELETE /api/cities/{id}
	     */
	    @DeleteMapping("/{id}")
	    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
	        boolean deleted = cityService.deleteCity(id);
	        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) 
	                      : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    }
	    

}
