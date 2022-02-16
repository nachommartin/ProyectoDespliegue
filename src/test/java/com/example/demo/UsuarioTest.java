package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.demo.model.Usuario;


class UsuarioTest {

	private static Usuario user;
	 @BeforeAll
	    public static void init() {
	        user = new Usuario();
	    }

	@Test
	void testGetNombre() {
		user.setNombre("Nacho");
		assertTrue(user.getNombre()=="Nacho");
	}
	
	@Test
	void testGetDireccion() {
		user.setDireccion("Calle de la Fresa");
		assertTrue(user.getDireccion()=="Calle de la Fresa");
	}

}
