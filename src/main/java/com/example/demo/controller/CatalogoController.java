package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.LineaPedido;
import com.example.demo.model.Pedido;
import com.example.demo.model.Producto;
import com.example.demo.model.Usuario;
import com.example.demo.services.PedidoService;
import com.example.demo.services.ProductoService;
import com.example.demo.services.UsuarioService;


@Controller
public class CatalogoController {
	
	@Autowired
	private HttpSession sesion;
	
	@Autowired
	private UsuarioService servicioUser;
	
	@Autowired
	private PedidoService servicioPed;
	
	@Autowired
	private ProductoService servicioPro;
	


	/**
	 * Página de inicio. Controlamos el acceso sin login al guardar el usuario en la sesión. Si está en medio del proceso de 
	 * realización del pedido ce intenta ir atrás sin finalizar el proceso, nos aseguramos el borrado
	 * @param model
	 * @return
	 */
	@GetMapping({"/inicioApp"})
	public String inicio(Model model) {
		if (sesion.getAttribute("userSaved")==null) {
			return "redirect:/forbidden";
		}
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		if (aux.getListaPedidos().size()>0) {
			if(servicioUser.ultimoPedido(aux.getNick()).isTramitado()==false) {
				Pedido ped = servicioUser.ultimoPedido(aux.getNick());
				servicioUser.borrarPedido(ped, aux.getNick());
			}
		}
		model.addAttribute("usuario", aux);
		return "inicioApp";
	}
	
	/**
	 * Página de catálogo. Si no hubiera sido tan perfeccionista para evitar que rompieran la app (saltándose el proceso si conoce
	 * las direcciones exactas) con está página me hubiera servido tanto para nuevos pedidos como para las ediciones. Pero ante la
	 * complejidad de evitar excepciones he tomado la decisión de hacer dos para cada funcionalidad, siendo éste para la creación 
	 * de nuevos pedidos, controlando esto último con el booleano isTramitado. Si el pedido existe (ya que antes de tramitar
	 * hemos dado la opción de volver atrás por si el usuario quiere retocar el pedido) recuperamos ese pedido y como no lo podemos
	 * guardar en la sesión, ordenamos nuestro Hash Set de pedidos y recuperamos el último, ya que se ordenan por fechas siendo el
	 * primer valor el más reciente, y si no existe, se crea. En esta página se carga el catálogo de productos de una colección.
	 * @param model
	 * @return
	 */
	@GetMapping({"/catalogo"})
	public String catalogo(Model model) {
		if (sesion.getAttribute("userSaved")==null) {
			return "redirect:/forbidden";
		}
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		Pedido ped;
		if (aux.getListaPedidos().size()>0) {
				ped = servicioUser.ultimoPedido(aux.getNick());
				if (ped.isTramitado()==false) {
					servicioUser.borrarPedido(ped, aux.getNick());
					ped =new Pedido(servicioUser.ultimoPedido(aux.getNick()).getReferencia()+1);
				}
				else {
					long anadir= servicioUser.ultimoPedido(aux.getNick()).getReferencia()+1;
					ped =new Pedido(anadir);
				
				}

		}
		else {
			ped = new Pedido(); 
		}
		servicioUser.addPedido(ped, aux.getNick());;
		Producto producto= new Producto();
		model.addAttribute("usuario", aux);
		model.addAttribute("productos", servicioPro.mostrarProductos());
		model.addAttribute("productoGenerado", producto);
		return "catalogo";
	}
	/**
	 * En el catálogo hay un formulario asociado a cada producto con un botón para añadir y otro para borrar. Pues aquí se
	 * recuperan los parámetros del botón añadir que van editando los atributos del objeto producto que se genera que se añade
	 * a la colección del pedido. La cantidad, un atributo del producto, tiene la validación de Spring requerida en el enunciado,
	 * controlando que esté entre 1 y 10 y que no sea nulo. Mientras se añaden productos se ofrece información del pedido y se 
	 * muestra el carrito actualizado
	 * @param model
	 * @param producto
	 * @param bindingResult
	 * @return
	 */
	@PostMapping(value="/catalogo", params={"anadir"} )
	public String submitCatalogo(Model model, @Valid @ModelAttribute("productoGenerado") Producto producto, 
			@RequestParam(value ="cantidad") int cantidad,	BindingResult bindingResult) {
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		model.addAttribute("usuario", aux);
		model.addAttribute("productos", servicioPro.mostrarProductos());
		model.addAttribute("productoGenerado", producto);
		if (bindingResult.hasErrors()) {
			String cadenaError = bindingResult.getFieldError().getDefaultMessage();
			model.addAttribute("mensajeError", cadenaError);
			return "catalogo";
		} 
		else {

				Pedido ped = servicioUser.ultimoPedido(aux.getNick());	
				if(servicioUser.recuperadorPedido(ped, aux.getListaPedidos())==null) {
					servicioPed.addProductos(ped, producto, cantidad);
					servicioUser.addPedido(ped, aux.getNick());

				}
				else {
					ped = servicioUser.recuperadorPedido(ped, aux.getListaPedidos());
					System.out.println(ped);
					servicioPed.addProductos(ped, producto, cantidad);
					servicioUser.addPedido(ped, aux.getNick());	
				}
				ArrayList<String> lista= ped.verCarrito();
				model.addAttribute("carrito", lista);
				ped.calcularCosteTotal();
				String total= ""+(double)Math.round(ped.getCoste() * 100d) / 100d;
				model.addAttribute("sumaCarrito",total);
			}
			
			return "catalogo";
		}
	
	/**
	 * Aquí se recupera el objeto creado en el formulario al pulsar el botón borrar y comprueba si está o no está en el 
	 * pedido con su respectivo mensaje de error si no está, si se han indicado más cantidad de la que está en el pedido. Como en 
	 * el caso anterior se van mostrando mediantem el envío de model mensajes sobre el pedido y se muestra el carrito. También tiene
	 * una validación con el atributo cantidad. 
	 * @param model
	 * @param producto
	 * @param bindingResult
	 * @return
	 */
	@PostMapping(value="/catalogo", params={"borrar"} )
	public String submitCatalogoBorrar(Model model, @Valid @ModelAttribute("productoGenerado") Producto producto,
			@RequestParam(value ="cantidad") int cantidad, BindingResult bindingResult) {
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		model.addAttribute("usuario", aux);
		model.addAttribute("productos", servicioPro.mostrarProductos());
		model.addAttribute("productoGenerado", producto);
		if (bindingResult.hasErrors()) {
			String cadenaError = bindingResult.getFieldError().getDefaultMessage();
			model.addAttribute("mensajeError", cadenaError);
			return "catalogo";
		} 
		else {

			Pedido ped= servicioUser.ultimoPedido(aux.getNick());
			if(servicioUser.recuperadorPedido(ped, aux.getListaPedidos())==null) {
				String auxCadena= "Tu carrito esta vacio";
				model.addAttribute("carritoNo",auxCadena);

			}
			else {
				ped = servicioUser.recuperadorPedido(ped, aux.getListaPedidos());
				String auxCadena= servicioPed.quitarProductos(ped, producto, cantidad);
				model.addAttribute("carritoNo",auxCadena);	
				servicioUser.addPedido(ped, aux.getNick());	
				if(ped.getLineaPedido().isEmpty()==false) {
					ArrayList<String> lista= ped.verCarrito();
					model.addAttribute("carrito", lista);
					ped.calcularCosteTotal();
					String total= ""+ped.getCoste();
					model.addAttribute("sumaCarrito",total);
				}
			}
			return "catalogo";
		}
	}
	
	/**
	 * Este es el catalogo hecho exclusivamente para edición. Para evitar que en mitad del proceso un usuario que conozca la ruta 
	 * y vaya hacia el Historial de Pedidos donde se mostrará el pedido guardado (ya que lo recuperamos, guárdándolo en la
	 * colección del usuario al no poder el guardar pedido en la sesión) se han añadido dos banderas para controlar siempre que si
	 * se truca el proceso al ir el historial se muestre el pedido tal y como estaba antes del proceso edición. Para ello
	 * se ha conseguido los atributos del pedido mediante métodos (ya que no se pueden hacer copias independientes en Java) y 
	 * esos atributos sí que se han guardado en sesión
	 * @param model
	 * @return
	 */
	@GetMapping({"/catalogoEdicion"})
	public String catalogoEdicion(Model model) {
		if (sesion.getAttribute("userSaved")==null) {
			return "redirect:/forbidden";
		}
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		if (aux.getListaPedidos().size()>0) {
			if(servicioUser.ultimoPedido(aux.getNick()).isTramitado()==true && servicioUser.ultimoPedido(aux.getNick()).isEditado()==false && servicioUser.ultimoPedido(aux.getNick()).getEditadoTramitado()==1);   {
				Pedido ped = servicioUser.ultimoPedido(aux.getNick());
				ped.actualizarFecha();
				ped.setGastosEnvio(0.0);
				ped.setEditado(true);
				ped.setEditadoTramitado(2);
				servicioUser.addPedido(ped, aux.getNick());
				ArrayList<String> lista= ped.verCarrito();
				String edicion="Vas a editar tu pedido";
				model.addAttribute("pedEdit", edicion);
				model.addAttribute("carrito", lista);
				ped.calcularCosteTotal();
				String total= ""+(double)Math.round(ped.getCoste() * 100d) / 100d;
				model.addAttribute("sumaCarrito",total);

			}
		}
		Producto producto= new Producto();
		model.addAttribute("usuario", aux);
		model.addAttribute("productos", servicioPro.mostrarProductos());
		model.addAttribute("productoGenerado", producto);
		return "catalogoEdicion";
	}
	/**
	 * El añadir del catálogo de edición con un funcionamiento idéntico al catálogo de creación de pedido
	 * @param model
	 * @param producto
	 * @param bindingResult
	 * @return
	 */
	@PostMapping(value="/catalogoEdicion", params={"anadir"} )
	public String submitCatalogoEdicion(Model model, @Valid @ModelAttribute("productoGenerado") Producto producto,
			@RequestParam(value ="cantidad") int cantidad, BindingResult bindingResult) {
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		model.addAttribute("usuario", aux);
		model.addAttribute("productos", servicioPro.mostrarProductos());
		model.addAttribute("productoGenerado", producto);
		if (bindingResult.hasErrors()) {
			String cadenaError = bindingResult.getFieldError().getDefaultMessage();
			model.addAttribute("mensajeError", cadenaError);
			return "catalogoEdicion";
		} 
		else {
			if(servicioUser.ultimoPedido(aux.getNick()).isTramitado()==true) {
				Pedido ped = servicioUser.ultimoPedido(aux.getNick());
				servicioPed.addProductos(ped, producto, cantidad);
				servicioUser.addPedido(ped, aux.getNick());
				ArrayList<String> lista= ped.verCarrito();
				model.addAttribute("carrito", lista);
				ped.calcularCosteTotal();
				String total= ""+ped.getCoste();
				model.addAttribute("sumaCarrito",total);
			}
			return "catalogoEdicion";
		}
	}
	/**
	 * El borrar del catálogo de edición con un funcionamiento idéntico al catálogo de creación de pedido
	 * @param model
	 * @param producto
	 * @param bindingResult
	 * @return
	 */
	@PostMapping(value="/catalogoEdicion", params={"borrar"} )
	public String submitCatalogoBorrarEdicion(Model model, @Valid @ModelAttribute("productoGenerado") Producto producto,
			@RequestParam(value ="cantidad") int cantidad, BindingResult bindingResult) {
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		model.addAttribute("usuario", aux);
		model.addAttribute("productos", servicioPro.mostrarProductos());
		model.addAttribute("productoGenerado", producto);
		if (bindingResult.hasErrors()) {
			String cadenaError = bindingResult.getFieldError().getDefaultMessage();
			model.addAttribute("mensajeError", cadenaError);
			return "catalogoEdicion";
		} 
		else {
			if(servicioUser.ultimoPedido(aux.getNick()).isTramitado()==true) {
				Pedido ped = servicioUser.ultimoPedido(aux.getNick());
				if(servicioUser.recuperadorPedido(ped, aux.getListaPedidos())==null) {
					String auxCadena= "Tu carrito esta vacio";
					model.addAttribute("carritoNo",auxCadena);
					
				}
			else {
				ped = servicioUser.recuperadorPedido(ped, aux.getListaPedidos());
				String auxCadena= servicioPed.quitarProductos(ped, producto, cantidad);
				model.addAttribute("carritoNo",auxCadena);	
				servicioUser.addPedido(ped, aux.getNick());	
				if(ped.getLineaPedido().isEmpty()==false) {
					ArrayList<String> lista= ped.verCarrito();
					model.addAttribute("carrito", lista);
					ped.calcularCosteTotal();
					String total= ""+ped.getCoste();
					model.addAttribute("sumaCarrito",total);
				}
			}
			}
			return "catalogoEdicion";
		}
	}
	/**
	 * Página para el envío. Se vuelve a controlar el acceso sin login y 
	 * vale tanto para indicar el envío y la dirección (que se da la
	 * opción de que se envie a la dirección del usuario) de un pedido
	 * de nueva creación o de un pedido en el historial que se va a editar
	 * (controlado esto último con banderas)
	 * @param model
	 * @return
	 */
	@GetMapping({"/envio"})
	public String envio(Model model) {
		if (sesion.getAttribute("userSaved")==null) {
			return "redirect:/forbidden";
		}
	Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
	Pedido ped = servicioUser.ultimoPedido(aux.getNick());
	if(ped.getEditadoTramitado()==1) {
	ped.setEditadoTramitado(2);
	}
	model.addAttribute("usuario",aux);
	return "envio";
	}
	/**
	 * En esta página se recupera los parámetros de dos radios para añadir
	 * o editar en el pedido la dirección de envío y los gastos de envío
	 * @param model
	 * @param precioASumar
	 * @param direccion
	 * @return
	 */
	@PostMapping({"/envio"})
	public String envioSubmit(Model model, @RequestParam(value ="precio", required = false) Double precioASumar,
			@RequestParam(value ="direccion", required = false) String direccion) {
	Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
	Pedido ped = servicioUser.ultimoPedido(aux.getNick());
	model.addAttribute("usuario",aux);
	model.addAttribute("precio",precioASumar);
	if(precioASumar!=null && !direccion.equals("")) {
		ped.setGastosEnvio(precioASumar);
		ped.setCoste((double)Math.round((ped.getCoste()+ped.getGastosEnvio()) * 100d) / 100d);
		if (direccion.startsWith(",")) {
			direccion=direccion.substring(1);
		}
		ped.setDireccion(direccion);
		servicioUser.addPedido(ped, aux.getNick());
		return "redirect:/factura";
	}
	else if(precioASumar==null && !direccion.equals("")) {
		precioASumar=0.25;
		model.addAttribute("precio",precioASumar);
		return "envio";
	}
	else {
		model.addAttribute("direccion",direccion);
		return "envio";
	}
	
	}
	/**
	 * Esta página es el resumen del pedido y en ella se cambian las
	 * banderas en los pedidos para evitar que un usuario si se salta
	 * el proceso no persistan los cambios (ya que la recuperación del
	 * pedido se hace accediendo al último añadido porque no lo guardamos
	 * en sesión). Se dan las opciones de tramitar el pedido (para que te
	 * muestre el historial o vuelva a empezar y te reenvíe al catálogo).
	 * Se controla el acceso sin haberse logueado. 
	 * @param model
	 * @return
	 */
	@GetMapping({"/factura"})
	public String facturar(Model model) {
		if (sesion.getAttribute("userSaved")==null) {
			return "redirect:/forbidden";
		}
	Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
	Pedido ped = servicioUser.ultimoPedido(aux.getNick());
	if (ped.getEditadoTramitado()==2) {
	ped.setEditadoTramitado(1);
	}
	ped.setEditado(false);
	servicioUser.addPedido(ped, aux.getNick());
	Double gastosEnvio = ped.getGastosEnvio(); 
	model.addAttribute("usuario",aux);
	model.addAttribute("pedido",ped);
	model.addAttribute("envio",gastosEnvio);
	String direccion = ped.getDireccion();
	model.addAttribute("dire",direccion);
	ArrayList<String> lista= ped.verCarrito();
	model.addAttribute("carrito", lista);
	String total= ""+ped.getCoste();
	model.addAttribute("sumaCarrito", total);
	return "factura";
	}
	/**
	 * La página que muestra el historial de pedidos del usuario. Se
	 * controla que no se acceda sin login y que el usuario no tenga
	 * pedido alguno en su historial. Para evitar que cuando un pedido
	 * esté en proceso y si se conoce la ruta se vaya a esta página sin
	 * haber finalizado la tramitación he hecho dos 'caminos': uno
	 * para los pedidos de nueva creación y otro para los editados con
	 * varias banderas para borrar los pedidos de nueva creación no
	 * finalizados o para volver a dejar como estaban los pedidos que se
	 * van a editar. 
	 * @param model
	 * @return
	 */
	@GetMapping({"/historial"})
		public String historial(Model model) {
		if (sesion.getAttribute("userSaved")==null) {
			return "redirect:/forbidden";
		}
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		if (aux.getListaPedidos().size()>0 && sesion.getAttribute("controlEditar")!=null && servicioUser.ultimoPedido(aux.getNick()).isEditado()==true) {
			Pedido ped = servicioUser.ultimoPedido(aux.getNick());
			Pedido antiguo= servicioPed.getByRef((long) sesion.getAttribute("controlEditar"));
			if (ped.getEditadoTramitado()==1) {
				ped.setEditadoTramitado(2);
			}
			else {
				ped.setEditadoTramitado(1);
			}
			if(ped.getGastosEnvio()==0.0 && antiguo.equals(ped)) {
				List<LineaPedido> copiaPro= (List<LineaPedido>) sesion.getAttribute("productos");
				long  periodo = (long) sesion.getAttribute("fechaPeriodo");
				Date auxFecha = ped.getFecha();
			    Calendar calendar = Calendar.getInstance();
			    calendar.setTime(auxFecha); // 
			    calendar.add(Calendar.DAY_OF_YEAR, (int) periodo);  
			    auxFecha= calendar.getTime();
				ped.setFecha(auxFecha);
				ped.setLineaPedido(copiaPro);
				ped.setGastosEnvio((double) sesion.getAttribute("gstEnvio")+1);
				ped.calcularCosteTotal();
				ped.setCoste(ped.getCoste()+ped.getGastosEnvio());
				ped.setEditado(false);
				ped.setTramitado(true);
				ped.setEditadoTramitado(0);
				servicioUser.addPedido(ped, aux.getNick());
			}
			else if(sesion.getAttribute("gstEnvio")!=null){
				Double gtsEnvioOld= (Double) sesion.getAttribute("gstEnvio");
				if(ped.getEditadoTramitado()==1 && ped.getGastosEnvio()!=0.0 && antiguo.equals(ped) && (gtsEnvioOld!=ped.getGastosEnvio() || !antiguo.getDireccion().equals(ped.getDireccion()))) {
					List<LineaPedido> copiaPro= (List<LineaPedido>) sesion.getAttribute("productos");
					long  periodo = (long) sesion.getAttribute("fechaPeriodo");
					LocalDateTime auxFecha = ped.getFecha().toInstant()
				      .atZone(ZoneId.systemDefault())
				      .toLocalDateTime();
					auxFecha= auxFecha.minusDays(periodo); 
					ped.setFecha(java.util.Date
						      .from(auxFecha.atZone(ZoneId.systemDefault())
						    	      .toInstant()));
					ped.setLineaPedido(copiaPro);
					ped.setGastosEnvio(1+(double) sesion.getAttribute("gstEnvio"));
					ped.calcularCosteTotal();
					ped.setCoste(ped.getCoste()+ped.getGastosEnvio());
					ped.setEditado(false);
					ped.setTramitado(true);
					ped.setEditadoTramitado(0);
					servicioUser.addPedido(ped, aux.getNick());
				}
				else if(ped.getEditadoTramitado()==2 && ped.getGastosEnvio()!=0.0 && antiguo.equals(ped) && (gtsEnvioOld==ped.getGastosEnvio() || antiguo.getDireccion().equals(ped.getDireccion()))) {
					ped.setEditado(false);
					ped.setTramitado(true);
					ped.setEditadoTramitado(0);
					servicioUser.addPedido(ped, aux.getNick());
				}
			}
		}
		if (aux.getListaPedidos().size()>0 && servicioUser.ultimoPedido(aux.getNick()).isEditado()==false && servicioUser.ultimoPedido(aux.getNick()).getEditadoTramitado()==0) {
			Pedido ped = servicioUser.ultimoPedido(aux.getNick());
			if (ped.getGastosEnvio()==0.0) {
				servicioUser.borrarPedido(ped, aux.getNick());
			}
			else {
				ped.setTramitado(true);
				servicioUser.addPedido(ped, aux.getNick());
			}
		}
		model.addAttribute("usuario", aux);
		model.addAttribute("pedidos", servicioUser.verPedidos(aux.getNick()));
		return "historial";
	}

	/**
	 * Recuperamos por parámetro la referencia del pedido que se está mostrando en el historial y aplicamos un método para borrarlo
	 * @param model
	 * @param refPedido
	 * @return
	 */
	@PostMapping("/historial")
	public String historialSubmit(Model model,@RequestParam(name ="idBorrado") long refPedido) {
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		Pedido ped = servicioPed.getByRef(refPedido);
		servicioUser.borrarPedido(ped, aux.getNick());
		model.addAttribute("usuario", aux);
		model.addAttribute("pedidos", servicioUser.verPedidos(aux.getNick()));
		return "historial";
	}
	/**
	 * Recuperamos la variable de la ruta que será la referencia del pedido que recuperamos por el servicio y es el pedido
	 * que se va a editar en el catálogo desarrollado para el proceso de edición
	 * @param id
	 * @param model
	 * @return
	 */
	@GetMapping("/historial/edit/{id}")
	public String editarPedodp(@PathVariable long id, Model model) {
		if (sesion.getAttribute("userSaved")==null) {
			return "redirect:/forbidden";
		}
		Usuario aux = servicioUser.getByNick(sesion.getAttribute("userSaved").toString());
		Pedido ped = servicioPed.getByRef(id);
		ped.setEditadoTramitado(1);
		Date fecha = ped.getFecha();
		Date fechaAhora= new Date();
		long milisegundos = Math.abs(fechaAhora.getTime() - fecha.getTime());
		long dias= TimeUnit.DAYS.convert(milisegundos, TimeUnit.MILLISECONDS);
		sesion.setAttribute("fechaPeriodo", dias);
		ped.actualizarFecha();
		List<LineaPedido> copia = servicioUser.copiadorProductos(ped);
		Double envio= servicioPed.controladorGastosEnvio(1.0,ped);
		sesion.setAttribute("gstEnvio", envio);
		sesion.setAttribute("productos", copia);
		sesion.setAttribute("controlEditar", ped.getReferencia());
		servicioUser.addPedido(ped, aux.getNick());
		return "redirect:/catalogoEdicion";
		
	}

}
