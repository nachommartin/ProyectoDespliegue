package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.LineaPedido;
import com.example.demo.model.Pedido;
import com.example.demo.model.Producto;
import com.example.demo.repository.LineaPedidoRepository;
import com.example.demo.repository.PedidoRepository;


@Service
public class PedidoService {
	
	@Autowired
	private LineaPedidoRepository repositorio;
	
	@Autowired
	private PedidoRepository repoPed;
	
	
	
	/**
	 * Método para extraer el pedido indicando su referencia
	 * @param user
	 * @param ref
	 * @return
	 */
	public Pedido getByRef(long ref) {
		return repoPed.findById(ref).orElse(null);
	}
	
	
	/**
	 * Añadir productos al pedido. Si el producto ya está en el pedido, se 
	 * aumenta de cantidad 
	 * @param aux
	 * @param producto
	 * @param cant
	 */
	public void addProductos(Pedido aux, Producto producto, int cant) {	
		LineaPedido lp = new LineaPedido (aux, producto, cant);
			if (aux.getLineaPedido().isEmpty()) {
				aux.getLineaPedido().add(lp);
				producto.getLineaPedido().add(lp);
			}
			else {
				if(aux.getLineaPedido().contains(lp)) {
				int OldLp = aux.getLineaPedido().indexOf(lp);
				int OldLpPro = producto.getLineaPedido().indexOf(lp);
				int cantAntigua = aux.getLineaPedido().get(OldLp).getCantidad();
				aux.getLineaPedido().get(OldLp).setCantidad(cant+cantAntigua);
				}
				else {
					aux.getLineaPedido().add(lp);
					producto.getLineaPedido().add(lp);
				}
		}
	}
	
	
	
	/**
	 * Se quita un producto del pedido. Se controla que el pedido tenga que estar en el pedido
	 * o que no se quite más cantidad de la que se tiene en el pedido
	 * @param aux
	 * @param producto
	 * @param cant
	 * @return
	 */
	public String quitarProductos(Pedido aux, Producto producto, int cant) {
		String cadena="";
		for(int i = 0;i<aux.getLineaPedido().size();i++) {
			if (aux.getLineaPedido().get(i).getProducto().equals(producto)) {
				int cantidad= aux.getLineaPedido().get(i).getCantidad()-cant;
				if (cantidad<0) {
					cadena="Has quitado más cantidad de ese producto del que tenías en el carrito";
				}
				else {
					aux.getLineaPedido().get(i).setCantidad(cantidad);
					repositorio.save(aux.getLineaPedido().get(i));
					cadena="Has quitado correctamente el producto";
					if(aux.getLineaPedido().get(i).getCantidad()==0) {
						aux.getLineaPedido().remove(aux.getLineaPedido().get(i));
					}
				}
			}
			else
			 {
				cadena="Ese producto no está en tu carrito";
			}
		}
			return cadena;
		}

	
	
	/**
	 * Método para recuperar los gastos de envio originales de un pedido que se va a editar
	 * pero el proceso de edición no ha terminado porque el usuario ha roto la aplicación poniendo
	 * una ruta a la que no debería acceder
	 * @param d
	 * @param ped
	 * @return
	 */
	public Double controladorGastosEnvio(Double d, Pedido ped) {
		Double resul=ped.getGastosEnvio()-d;
		return resul;
	}

}