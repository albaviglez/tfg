package modelo;

import comunicacion.BDConexion;
import org.json.simple.JSONArray;

public abstract class Usuario {

    protected String _clave, _apellidos, _nombre, _usuario, _email, _telefono, id;
    protected BDConexion bdconexion;

    public abstract void salir();

    public abstract JSONArray entrar(String aUsuario, String aClave);
}
