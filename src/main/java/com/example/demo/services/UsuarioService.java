package com.example.demo.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.example.demo.model.LineaPedido;
import com.example.demo.model.Pedido;
import com.example.demo.model.Producto;
import com.example.demo.model.Usuario;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	@Autowired
	private PedidoService servicioPed;
	
	@Autowired
	private ProductoService servicioPro;
	
	@Autowired
	private UsuarioRepository repositorio;
	
	@Autowired
	private PedidoRepository repoPed;
	
	/**
	 * Recuperación del usuario mediante su hashcode: su nick
	 * @param nick
	 * @return
	 */
	public Usuario getByNick(String nick) {
		return repositorio.findById(nick).orElse(null);
	}
	
	/**
	 * Comprobar que el usuario introducido está en el repositorio
	 * @param nick
	 * @return
	 */
	public boolean comprobarUsuario(String nick){
		boolean resul = false;
		if (repositorio.getById(nick)!=null) {
				resul = true;
			}	
		return resul;
	}
	
	/**
	 * Comprobar que la contraseña introducida está en el repositorio
	 * @param pass
	 * @return
	 */
	public boolean comprobarPass(String nick, String pass){
		boolean resul = false;
		if (repositorio.getById(nick)!=null) {
			Usuario aux=  repositorio.getById(nick);
			if (aux.getPassword().equals(pass)) {
				resul=true;
			}
		}
		return resul;
	}
	
	/**
	 * Método para comprobar si se ha introducido las contraseña y el usuario correctamente, el usuario correcto pero
	 * la contraseña no y, por último, si la contraseña es correcta pero el usuario no o ambas no son correctas
	 * @param usuario
	 * @param contrasena
	 * @return
	 */
	public int comprobadorTotal(String usuario, String contrasena) {
		int resul; 
		try {
		if (this.comprobarUsuario(usuario) && this.comprobarPass(usuario, contrasena)==true) {
			resul= 1; 				
		}
		else if (this.comprobarUsuario(usuario)==true && this.comprobarPass(usuario,contrasena)==false){
			resul=0; 
		}
		else {
			resul=-1; 
		}
		return resul; 
		}
		catch(EntityNotFoundException e) {
			return -1;
		}
	}
	
	/**
	 * Se añade el pedido al usuario, y si ya existe en la colección se actualiza
	 * @param pedido
	 * @param nick
	 */
	public void addPedido(Pedido pedido, String nick) {
		Usuario aux = this.getByNick(nick);
		if(aux.getListaPedidos().contains(pedido) && pedido.equals(this.ultimoPedido(aux.getNick()))) {
			aux.getListaPedidos().remove(pedido);
			aux.getListaPedidos().add(pedido);	
			repositorio.save(aux);
		}
		else {
			aux.getListaPedidos().add(pedido);
			repositorio.save(aux);
		}
	}
	
	/**
	 * Mostrar el listado de pedidos de un usuario ordenados por fecha siendo los más recientes los primeros
	 * @param nick
	 * @return
	 */
	public List<Pedido> verPedidos(String nick) {
		Usuario aux = this.getByNick(nick);
		ArrayList<Pedido> aOrdenar = new ArrayList<Pedido>(aux.getListaPedidos());
		Collections.sort(aOrdenar);
		return aOrdenar;
	}
	
	/**
	 * Método para borrar un pedido
	 * @param pedido
	 * @param nick
	 */
	public void borrarPedido(Pedido pedido, String nick) {
		Usuario aux= this.getByNick(nick);
		aux.getListaPedidos().remove(pedido);
		repositorio.save(aux);
	}
	
	/**
	 * Método utilizado para recuperar el pedido cuando el usuario quiere borrar productos en el carrito
	 * @param pedido
	 * @param lista
	 * @return
	 */
	public Pedido recuperadorPedido(Pedido pedido, List<Pedido> lista){
		   if (lista.contains(pedido)) {
			   for (Pedido ped : lista) {
				   if (ped.equals(pedido)) {
					   return ped;
			        } 
			   }
		   }
		   return null;
	}
	
	/**
	 * Método para recuperar el último pedido que se está gestionando. Viene a paliar el requisito de no 
	 * poder guardar el pedido en sesión
	 * @param nick
	 * @return
	 */
	public Pedido ultimoPedido(String nick) {
		Usuario aux = this.getByNick(nick);
		ArrayList<Pedido> aOrdenar = new ArrayList<Pedido>(aux.getListaPedidos());
		Collections.sort(aOrdenar);
		Pedido ped = aOrdenar.get(0);
		return ped;
	}
	
	/**
	 * Método para recuperar la colección original de productos de un pedido que va a ser editado pero que el
	 * proceso no se ha completado porque el usuario ha trucado el proceso y así evitamos que no persista los cambios
	 * durante la edición
	 * @param ped
	 * @return
	 */
	public List<LineaPedido> copiadorProductos(Pedido ped) {
		List<LineaPedido> lista = new ArrayList<LineaPedido>();
		Iterator<LineaPedido> pr = lista.iterator();
	    while(pr.hasNext()) {
	    	LineaPedido aux= pr.next();
	       	lista.add(aux);
	    }
		return lista;
	}
	
	/**
	 * Creación de usuarios y pedidos para uno de ellos para agregarlos en el repositorio de usuarios
	 */
	@Bean
	@PostConstruct
	public void inicio() {
		repositorio.saveAll(
				Arrays.asList(new Usuario("nach85", "akira", "Nacho M. Martín", "Plaza Cronista 6, Sevilla", "656777888", "nacho@gmail.com"),
						new Usuario("powerh", "kirby", "Hugo Mateo", "Plaza Cronista 6, Sevilla", "656111222", "powerofh@yahoo.com"),
						new Usuario("nani98", "kanoute", "Fernando Campos", "Avenida Donantes de Sangre 23, Sevilla", "656444555", "ernani@hotmail.es")
						)
				);
		
		Date fecha1 = new Date(121, 9, 30,18,00);
		Pedido ped= new Pedido("Plaza Cronista 6", fecha1);
		Producto p1 = servicioPro.getByRef("007");
		Producto p2= servicioPro.getByRef("003");
		ped.setTramitado(true);
		ped.setGastosEnvio(1.99);
		servicioPed.addProductos(ped, p1, 1);
		servicioPed.addProductos(ped, p2, 1);
		ped.calcularCosteTotal();
		ped.setCoste(ped.getCoste()+ped.getGastosEnvio());
		repoPed.save(ped);
		addPedido(ped,"nach85");
		Date fecha2 = new Date(121, 10, 13, 6,30);
		Pedido ped2= new Pedido("Plaza Cronista 6", fecha2);
		ped2.setTramitado(true);
		ped2.setGastosEnvio(1.99);
		Producto p3 = servicioPro.getByRef("005");
		repoPed.save(ped);
		servicioPed.addProductos(ped2, p3, 2);
		ped2.calcularCosteTotal();
		ped2.setCoste(ped2.getCoste()+ped2.getGastosEnvio());
		addPedido(ped2,"nach85");	
	}


}
