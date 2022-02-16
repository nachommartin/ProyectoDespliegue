package com.example.demo.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.model.Usuario;
import com.example.demo.services.UsuarioService;

@Controller
public class LoginController {
	
	@Autowired
	private HttpSession sesion;


	@Autowired
	private UsuarioService servicio;
	
	/**
	 * Página de login
	 * @param model
	 * @return
	 */
	@GetMapping({"/"})
	public String inicio(Model model) {
		if (sesion.getAttribute("userSaved")!=null) {
			sesion.removeAttribute("userSaved");
		}
		model.addAttribute("usuario", new Usuario());
		return "login";
	}
	
	/**
	 * Si se ingresa con el usuario y la contraseña correcta entra en la propia aplicación. En caso contrario se deriva a
	 * la página de errores controlando tanto el usuario como la contraseña introducida
	 * @param aux
	 * @return
	 */
	@PostMapping("/")
	public String nuevoEmpleadoSubmit(@ModelAttribute Usuario aux) {
			if (servicio.comprobadorTotal(aux.getNick(), aux.getPassword())==1) {
				sesion.setAttribute("userSaved", aux.getNick());
				return "redirect:/inicioApp";
			} 
			else if(servicio.comprobadorTotal(aux.getNick(), aux.getPassword())==0) {
				sesion.setAttribute("userSaved", aux.getNick());
				return "redirect:/errores";
			}
			else {
				return "redirect:/errores";
			}
	}
	
	/**
	 * Página de error de login. Si el usuario es correcto pero la contraseña no, se indica que se ha equivocado y
	 * que vuelva a intentarlo, en el caso de que los dos parámetros o el usuario sea incorrecto, se le informa de ello 
	 * @param model
	 * @return
	 */
	@GetMapping({"/errores"})
	public String errorLogin(Model model) {
		Usuario aux = new Usuario();
		if (sesion.getAttribute("userSaved")==null) {
			model.addAttribute("usuario", aux);
			sesion.setAttribute("userSaved", "borrar");
		}
		else {
			aux.setNick(sesion.getAttribute("userSaved").toString());
			model.addAttribute("usuario", aux);
		}
		sesion.removeAttribute("userSaved");
		return "errorLogin";
	}

	@GetMapping({"/forbidden"})
	public String forbidden() {		
		return "forbidden";
	}



}
