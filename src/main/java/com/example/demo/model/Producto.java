package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;


/**
 * Como se tiene que chequar con el Validation la cantidad, esta es un atributo
 * del producto y tiene las anotaciones de que debe estar entre 1 y 10, ambos inclusive
 * y no ser nulo
 * @author humat
 *
 */

@Entity
public class Producto {
	
	@Id
	private String referenciaProducto;
	
	private String titulo;
	
	private String plataforma;
	
	@Column(updatable=true)
	private double precio;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy="producto", cascade= CascadeType.ALL, orphanRemoval=true)
	@Column(updatable=true)
	private List<LineaPedido> lineasPedido; 
	
	
	public Producto(String ref, String titulo, String plataforma, double precio) {
		super();
		this.referenciaProducto=ref;
		this.titulo = titulo;
		this.plataforma= plataforma;
		this.precio = precio;
		this.lineasPedido= new ArrayList<LineaPedido>();
	}

	
	public Producto() {
		super();
		this.lineasPedido= new ArrayList<LineaPedido>();
	}
	



	public String getReferenciaProducto() {
		return referenciaProducto;
	}



	public String getTitulo() {
		return titulo;
	}



	public double getPrecio() {
		return precio;
	}
	
	
	public String getPlataforma() {
		return plataforma;
	}
	
	public List<LineaPedido> getLineaPedido() {
		return lineasPedido;
	}




	public void setLineaPedido(List<LineaPedido> lineaPedido) {
		this.lineasPedido = lineaPedido;
	}	


	public void setReferenciaProducto(String referenciaProducto) {
		this.referenciaProducto = referenciaProducto;
	}


	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}


	public void setPlataforma(String plataforma) {
		this.plataforma = plataforma;
	}


	public void setPrecio(double precio) {
		this.precio = precio;
	}


	@Override
	public String toString() {
		return "Producto [referenciaProducto=" + referenciaProducto + ", titulo=" + titulo + ", plataforma="
				+ plataforma + ", precio=" + precio + "";
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(referenciaProducto);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Producto other = (Producto) obj;
		return Objects.equals(referenciaProducto, other.referenciaProducto);
	}

	

}
