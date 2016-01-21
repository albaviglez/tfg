package modelo;

import comunicacion.BDConexion;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Administrador extends Usuario {

    private ArrayList<String> resultado;
    private JSONArray info;
    // objeto para almacenar cada campo de la informacion
    private JSONObject datos;
    // directorio donde se almacenan los archivos .txt que contienen las conversaciones
    private final String directorio;
    // lista con todos los archivos .txt que hay en el directorio
    private File[] listaarchivos;

    public Administrador() {
        bdconexion = new BDConexion();
        directorio = "C:/xampp/tomcat/webapps/PFG/src/main/webapp/servidor/conversaciones";
    }

    @Override
    public JSONArray entrar(String aUsuario, String aClave) {
        // informacion del administrador
        info = new JSONArray();
        // se conecta a la BD
        if (bdconexion.conectar()) {
            // hace el login
            resultado = bdconexion.login(aUsuario, aClave);
            bdconexion.desconectar();
            if (resultado != null) {
                // login correcto
                for (int i = 0; i < resultado.size(); i++) {
                    info.add(resultado.get(i));
                }
                id = resultado.get(0);
                _usuario = resultado.get(1);
                _clave = resultado.get(2);
                _nombre = resultado.get(3);
                _apellidos = resultado.get(4);
                _email = resultado.get(5);
                _telefono = resultado.get(6);
                return info;
            } else {
                // error en el login
                info.add("error");
                info.add(" Error, el usuario o clave no existe.");
                return info;
            }
        } else {
            // error conectando a la BD
            info.add("error");
            info.add(" Error, fallo la conexión a la BD.");
            return info;
        }
    }

    @Override
    public void salir() {
        bdconexion.desconectar();
    }

    public boolean registrarAsesor(String aNombre, String aApellidos, String aEmail, String aTelefono, String aUsuario, String aClave, String idasesor) {
        // se conecta a la BD
        if (bdconexion.conectar()) {
            if (bdconexion.insertar("insert into asesores (`idasesor`,`usuario`,`clave`,`nombre`,`apellidos`,`email`,`telefono`)"
                    + " values('" + idasesor + "','" + aUsuario + "','" + aClave + "','" + aNombre + "','" + aApellidos + "','" + aEmail
                    + "','" + aTelefono + "')")) {
                bdconexion.desconectar();
                return true;
            } else {
                // error en la consulta
                bdconexion.desconectar();
                return false;
            }
        } else {
            // error conectando a la BD
            return false;
        }
    }

    public JSONArray cargarAsesor(String idasesor) {
        // informacion del asesor
        info = new JSONArray();
        // arrays para almacenar cada informacion por separado
        JSONArray clientes, conversaciones, conexiones;
        // para leer los ficheros que contienen las conversaciones
        BufferedReader bufferlectura;
        // para construir la conversacion
        String linea, conversacion;
        // se conecta a la BD
        if (bdconexion.conectar()) {
            try {
                // obtiene las conversaciones del asesor
                resultado = bdconexion.cargar("select * from conversaciones where idconversacion like '" + idasesor + "%'", 2);
                conversaciones = new JSONArray();
                // carga todas las conversaciones del asesor
                for (int i = 0; i < resultado.size(); i += 2) {
                    // objeto para cada conversacion
                    datos = new JSONObject();
                    datos.put("idconversacion", resultado.get(i));
                    // a partir de la ubicacion lee todos los mensajes de la conversacion
                    bufferlectura = new BufferedReader(new InputStreamReader(new FileInputStream(resultado.get(i + 1)), "utf-8"));
                    linea = bufferlectura.readLine();
                    conversacion = "";
                    while (linea != null) {
                        conversacion += linea;
                        linea = bufferlectura.readLine();
                        if (linea != null) {
                            // la conversacion tiene un salto de linea
                            conversacion += "<>";
                        } else {
                            // termina la conversacion
                            conversacion += "<<";
                        }
                    }
                    bufferlectura.close();
                    // añade la conversacion
                    datos.put("conversacion", conversacion);
                    conversaciones.add(datos);
                }
                info.add(conversaciones);

                // carga los clientes relacionados con el asesor
                clientes = new JSONArray();
                // busca a cada cliente que tuvo una conversacion con el asesor
                for (int i = 0; i < conversaciones.size(); i++) {
                    // obtiene cada conversacion del asesor
                    datos = (JSONObject) conversaciones.get(i);
                    // extrae el idcliente del idconversacion para buscar al cliente
                    linea = datos.get("idconversacion").toString().substring(datos.get("idconversacion").toString().indexOf("c"), datos.get("idconversacion").toString().length());
                    resultado = bdconexion.cargar("select * from clientes where idcliente='" + linea + "'", 3);
                    for (int j = 0; j < resultado.size(); j += 3) {
                        // objeto para cada conversacion
                        datos = new JSONObject();
                        datos.put("idcliente", resultado.get(j));
                        datos.put("nombre", resultado.get(j + 1));
                        datos.put("email", resultado.get(j + 2));
                        clientes.add(datos);
                    }
                }
                info.add(clientes);

                // carga las conexiones del asesor
                resultado = bdconexion.cargar("select * from conexiones where idasesor='" + idasesor + "'", 4);
                conexiones = new JSONArray();
                for (int i = 0; i < resultado.size(); i += 4) {
                    // objeto para cada conversacion
                    datos = new JSONObject();
                    datos.put("idconexion", resultado.get(i));
                    datos.put("idasesor", resultado.get(i + 1));
                    datos.put("inicio", resultado.get(i + 2));
                    datos.put("fin", resultado.get(i + 3));
                    conexiones.add(datos);
                }
                info.add(conexiones);
                bdconexion.desconectar();
                return info;
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Administrador.class.getName()).log(Level.SEVERE, null, ex);
                bdconexion.desconectar();
                info.add("error");
                info.add(" Error, UnsupportedEncodingException");
                return info;
            } catch (IOException ex) {
                Logger.getLogger(Administrador.class.getName()).log(Level.SEVERE, null, ex);
                bdconexion.desconectar();
                info.add("error");
                info.add(" Error, IOException");
                return info;
            }
        } else {
            // error conectando a la BD
            info.add("error");
            info.add(" Error, fallo la conexión a la BD.");
            return info;
        }
    }

    public boolean eliminarAsesor(String idasesor, String[] idclientes) {
        // se conecta a la BD
        if (bdconexion.conectar()) {
            // elimina al asesor con todas sus conversaciones y conexiones (lo hace automatico la BD por la relacion)
            if (bdconexion.insertar("delete from asesores where idasesor='" + idasesor + "'") && bdconexion.insertar("delete from conversaciones where idconversacion like '" + idasesor + "%'") && bdconexion.insertar("delete from conexiones where idasesor='" + idasesor + "'")) {
                // elimina los clientes relacionados con el asesor eliminado
                for (int i = 0; i < idclientes.length; i++) {
                    bdconexion.insertar("delete from clientes where idcliente='" + idclientes[i] + "'");
                }
                bdconexion.desconectar();
                // tambien elimina los archivos que contienen las conversaciones del servidor
                listaarchivos = new File(directorio).listFiles();
                // elimina todos los que esten relacionados con el asesor en cuestion
                for (int i = 0; i < idclientes.length; i++) {
                    for (int j = 0; j < listaarchivos.length; j++) {
                        // forma el nombre del archivo para ver si corresponde con el asesor y eliminarlo
                        if (listaarchivos[j].getName().equals(idasesor + idclientes[i] + ".txt")) {
                            listaarchivos[j].delete();
                            break;
                        }
                    }
                }
                return true;
            } else {
                // error en la consulta SQL
                return false;
            }
        } else {
            // error conectando a la BD
            return false;
        }
    }

    public boolean borrarConversaciones(String idasesor, String[] idclientes) {
        // se conecta a la BD
        if (bdconexion.conectar()) {
            // elimina todas las conversaciones del asesor de la BD
            if (bdconexion.insertar("delete from conversaciones where idconversacion like '" + idasesor + "%'")) {
                // elimina los clientes relacionados con el asesor
                for (int i = 0; i < idclientes.length; i++) {
                    bdconexion.insertar("delete from clientes where idcliente='" + idclientes[i] + "'");
                }
                bdconexion.desconectar();
                // tambien elimina los archivos que contienen las conversaciones del servidor
                // hace una lista con todos los archivos que hay en el directorio
                listaarchivos = new File(directorio).listFiles();
                // elimina todos los que esten relacionados con el asesor en cuestion
                for (int i = 0; i < idclientes.length; i++) {
                    for (int j = 0; j < listaarchivos.length; j++) {
                        // forma el nombre del archivo para ver si corresponde con el asesor y eliminarlo
                        if (listaarchivos[j].getName().equals(idasesor + idclientes[i] + ".txt")) {
                            listaarchivos[j].delete();
                            break;
                        }
                    }
                }
                return true;
            } else {
                // error en la consulta SQL
                return false;
            }
        } else {
            // error conectando a la BD
            return false;
        }
    }

    public boolean borrarConexiones(String idasesor) {
        // se conecta a la BD
        if (bdconexion.conectar()) {
            // elimina todas las conexiones del asesor
            if (bdconexion.insertar("delete from conexiones where idasesor='" + idasesor + "'")) {
                bdconexion.desconectar();
                return true;
            } else {
                // error en la consulta SQL
                bdconexion.desconectar();
                return false;
            }
        } else {
            // error conectando a la BD
            return false;
        }
    }

    public boolean editarAsesor(String aIdasesor, String aNombre, String aApellidos, String aUsuario, String aClave, String aEmail, String aTelefono) {
        // se conecta a la BD
        if (bdconexion.conectar()) {
            if (bdconexion.insertar("update asesores set nombre='" + aNombre + "', apellidos='" + aApellidos + "', usuario='" + aUsuario + "', clave='" + aClave + "', email='" + aEmail + "', telefono='" + aTelefono + "' where idasesor='" + aIdasesor + "'")) {
                bdconexion.desconectar();
                return true;
            } else {
                // error en la consulta SQL
                bdconexion.desconectar();
                return false;
            }
        } else {
            // error conectando a la BD
            return false;
        }
    }

    public JSONArray cargarAsesores() {
        // informacion de los asesores
        info = new JSONArray();
        // se conecta a la BD
        if (bdconexion.conectar()) {
            // error en la consulta SQL

            resultado = bdconexion.cargar("select * from asesores", 7);
            for (int i = 0; i < resultado.size(); i += 7) {
                datos = new JSONObject();
                datos.put("nombre", resultado.get(i + 3));
                datos.put("apellidos", resultado.get(i + 4));
                datos.put("idasesor", resultado.get(i));
                datos.put("usuario", resultado.get(i + 1));
                datos.put("clave", resultado.get(i + 2));
                datos.put("email", resultado.get(i + 5));
                datos.put("telefono", resultado.get(i + 6));
                info.add(datos);
            }
            bdconexion.desconectar();
            return info;
        } else {
            // error conectando a la BD
            info.add("error");
            info.add(" Error, fallo la conexión a la BD.");
            return info;
        }
    }

}

