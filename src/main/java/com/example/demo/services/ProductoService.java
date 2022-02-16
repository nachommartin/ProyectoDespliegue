package com.example.demo.services;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;

@Service
public class ProductoService {
	
	@Autowired
	private ProductoRepository repositorio;
	
	public Producto add(Producto pr) {
		return repositorio.save(pr);
	}

	public Producto getByRef(String ref) {
		return repositorio.findById(ref).orElse(null);
	}

	public List<Producto> mostrarProductos() {
		return repositorio.findAll();
	}
	
	@Bean
	@PostConstruct
	public void init() {
		repositorio.saveAll(
				Arrays.asList(new Producto("001", "Sonic The Hedgehog", "Mega Drive", 29.99),
						new Producto("002", "Strider", "Mega Drive", 39.99),
						new Producto("003", "Quackshot", "Mega Drive", 19.99),
						new Producto("004", "Flashback", "Mega Drive", 19.99),
						new Producto("005", "Super Mario World", "Super NES", 24.99),
						new Producto ("006", "Street Fighter II", "Super NES", 29.99),
						new Producto ("007", "Megaman 2", "NES", 19.99),
						new Producto ("008", "Kirby Dream Land", "Game Boy", 24.99)
						)
				);
		

	}



}
