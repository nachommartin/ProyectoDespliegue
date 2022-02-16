package com.example.demo.model;

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;

@Entity
public class LineaPedido {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long codigo; 
	
	@ManyToOne
    @JoinColumn(name = "pedido_id")
	private Pedido pedido;
	
	@ManyToOne
    @JoinColumn(name = "producto_id")
	private Producto producto; 
	
	/*
	@NotNull
	@Min(1)
	@Max(10)*/
	private int cantidad;
	
	
	

	public LineaPedido(Pedido pedido, Producto producto, int cantidad) {
		super();
		this.pedido = pedido;
		this.producto = producto;
		this.cantidad = cantidad;
	}
	
	

	public LineaPedido() {
		super();
	}

	

	@Override
	public int hashCode() {
		return Objects.hash(pedido, producto);
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineaPedido other = (LineaPedido) obj;
		return Objects.equals(pedido, other.pedido) && Objects.equals(producto, other.producto);
	}



	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	} 
	
	
	
	
	
	

}
