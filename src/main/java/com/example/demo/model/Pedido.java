package com.example.demo.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Hay tres banderas para controlar que si el proceso de edición o creación de un pedido no se
 * tramita correctamente, los cambios no persistan en el usuario
 * @author humat
 *
 */

@Entity
public class Pedido implements Comparable<Pedido> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long referencia=0;
	 
	@Column(updatable=true)
	private String direccion;
	
	@OneToMany(mappedBy="pedido", cascade = CascadeType.ALL, orphanRemoval=true)
	@Column(updatable=true)
	private List<LineaPedido> lineasPedido; 
	
	@Column(updatable=true)
	private double coste;
	
	@Temporal(TemporalType.DATE)
	@Column(updatable=true)
	private Date fecha; 
	
	@Column(updatable=true)
	private double gastosEnvio;
	
	@Column(updatable=true)
	private boolean tramitado;
	
	@Column(updatable=true)
	private boolean editado;
	
	@Column(updatable=true)
	private int editadoTramitado;
	
	
	public Pedido(String direccion, Date fecha) {
		super();
		this.direccion = direccion;
		this.coste = 0;
		this.fecha= fecha;
		this.gastosEnvio=0;
		this.tramitado=false;
		this.editado=false;
		this.editadoTramitado=0;
		this.lineasPedido= new ArrayList<LineaPedido>();
	} 
	
	
	
	public Pedido() {
		super();
		this.coste = 0;
		this.fecha = new Date();
		this.gastosEnvio=0;
		this.tramitado=false;
		this.editado=false;
		this.editadoTramitado=0;
		this.lineasPedido= new ArrayList<LineaPedido>();
	} 
	
	public Pedido(long referencia) {
		super();
		this.referencia= referencia;
		this.fecha = new Date();
		this.tramitado=false;
		this.editado=false;
		this.editadoTramitado=0;
		this.lineasPedido= new ArrayList<LineaPedido>();

	}
	
	
	
		
	public long getReferencia() {
		return referencia;
	}



	public void setReferencia(long referencia) {
		this.referencia = referencia;
	}



	
	public double getGastosEnvio() {
		return gastosEnvio;
	}



	public void setGastosEnvio(double gastosEnvio) {
		this.gastosEnvio = gastosEnvio;
	}



	public String getDireccion() {
		return direccion;
	}



	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}



	public List<LineaPedido> getLineaPedido() {
		return lineasPedido;
	}






	public boolean isEditado() {
		return editado;
	}



	public void setEditado(boolean editado) {
		this.editado = editado;
	}



	public double getCoste() {
		return coste;
	}



	public void setLineaPedido(List<LineaPedido> lineaPedido) {
		this.lineasPedido = lineaPedido;
	}



	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}



	public boolean isTramitado() {
		return tramitado;
	}



	public void setTramitado(boolean tramitado) {
		this.tramitado = tramitado;
	}



	public void setCoste(double coste) {
		this.coste = coste;
	}

	
	public Date getFecha() {
		return fecha;
	}
	
	
	/**
	 * El set de la fecha es actualizarla al momento actual para el proceso de edición
	 */
	public void actualizarFecha() {
		this.fecha=new Date();
	}
	
	
	

	public int getEditadoTramitado() {
		return editadoTramitado;
	}



	public void setEditadoTramitado(int editadoTramitado) {
		this.editadoTramitado = editadoTramitado;
	}



	@Override
	public int hashCode() {
		return Objects.hash(referencia);
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pedido other = (Pedido) obj;
		return referencia == other.referencia;
	}



	/**
	 * Método para calcular el coste del carrito del pedido (precio producto * cantidad)
	 */
	public void calcularCosteTotal(){
		double contador =0;
		
		Iterator<LineaPedido> lp = this.lineasPedido.iterator();
        while(lp.hasNext()) {
        	LineaPedido aux= lp.next();
	        contador+= (aux.getProducto().getPrecio()*aux.getCantidad()); 			
			}
		this.coste= Math.round(contador * 100d) / 100d;
	}
	
	@Override
	public String toString() {
		String resul; 
		String auxFecha;
		StringBuilder cadena= new StringBuilder();
		Iterator<LineaPedido> lp = this.lineasPedido.iterator();
	    while(lp.hasNext()) {
	       	LineaPedido aux= lp.next();
	        cadena.append(aux.getCantidad() + " unidad(es) de " + aux.getProducto().getTitulo()+" a "+ aux.getProducto().getPrecio() +" euros ");
			}
	    	SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
		    auxFecha = dt.format(this.fecha);
	    	resul = cadena.toString();	  
		return "Pedido: "+ this.referencia + " Fecha: " + auxFecha +" Precio: " + this.coste+ " euros Productos: " + resul;
	}
	/**
	 * Es parecido al To String pero si este último se utiliza para mostrar el pedido en el historial, éste se usa 
	 * para mostrar el carrito durante el proceso de creación/edición del pedido
	 * @return
	 */
	public ArrayList<String> verCarrito() {
		ArrayList<String> listado= new ArrayList<String>();
		String resul; 
		Iterator<LineaPedido> lp = this.lineasPedido.iterator();
	    while(lp.hasNext()) {
	       	LineaPedido aux= lp.next();
	        resul= aux.getCantidad() + " unidad(es) de " + aux.getProducto().getTitulo()+" de "+aux.getProducto().getPlataforma()+" a "+ aux.getProducto().getPrecio() +" euros ";
	        listado.add(resul);
			}
		return listado;
	}



	/**
	 * Comparador mediante la fecha
	 */
	@Override
	public int compareTo(Pedido ped) {
		// TODO Auto-generated method stub
		int resul; 
		if (ped.getFecha().equals(this.fecha)) {
			resul=0;
		}
		else if (ped.getFecha().after(this.fecha)) {
			resul=1;
		}
		else {
			resul=-1;
		}
		return resul;
	}
	


}
