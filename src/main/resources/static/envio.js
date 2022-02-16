function mostrar() {
	  let opcion = document.getElementById("otraDire");
	  let inputAMostrar= document.getElementById("escondido");
	  if (opcion.checked) {
	    inputAMostrar.style.display = "block";
	  } 
	  else {
	    inputAMostrar.style.display = "none";
	  }
	} 