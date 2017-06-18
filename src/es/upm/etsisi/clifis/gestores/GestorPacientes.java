/*
        Clifis v1.0b (c) 2017 Escuela Técnica Superior de Ingeniería de Sistemas Informáticos (UPM)

        This file is part of Clifis.

        Clifis is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        Clifis is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with Clifis.  If not, see <http://www.gnu.org/licenses/>.
*/

package es.upm.etsisi.clifis.gestores;

import es.upm.etsisi.clifis.model.Paciente;
import es.upm.etsisi.clifis.model.PacienteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona los pacientes con respecto al gestor de bases de datos.
 */
public class GestorPacientes  implements Serializable {

    private final static Logger LOG = LoggerFactory.getLogger("es.upm.etsisi.clifis.gestores.GestorPacientes");

    /**
     * Da de alta un paciente.
     *
     * @param paciente Contiene la información del paciente a dar de alta.
     */
    public void altaPaciente (Paciente paciente) throws GestorException {
        String error ="";
        if(paciente == null){
            System.out.println("PACIENTE VACIO");
            throw new GestorException("Paciente vacío");
        }
        if(paciente.getIdSeg() == null || paciente.getIdSeg().isEmpty())
            error += "Error de DNI. ";
        if(paciente.getNombre() == null || paciente.getNombre().isEmpty())
            error+="Error de nombre. ";
        if(paciente.getApellidos() == null || paciente.getApellidos().isEmpty())
            error+="Error de apellidos. ";
        if(paciente.getAseguradora() == null || paciente.getAseguradora().isEmpty())
            error+="Error de aseguradora. ";
        if(!error.isEmpty())
            throw new GestorException(error);

        try (Connection con = DBManager2.getConn()){
            PreparedStatement pstm = con.prepareStatement(
                    "INSERT INTO Paciente (" +
                            "Pac_IDSEG , Pac_Nombre , Pac_Apellidos, Pac_SegMedico) " +
                            "VALUES" +
                            " ( ?, ?, ?, ?)"
            );
            pstm.setString(1,paciente.getIdSeg());
            pstm.setString(2,paciente.getNombre());
            pstm.setString(3,paciente.getApellidos());
            pstm.setString(4,paciente.getAseguradora());

            pstm.execute();
            pstm.close();

        } catch (SQLException e) {
            LOG.debug("Controlada una SQLException al dar de alta un paciente." , e);
            if(e.getMessage().contains("Duplicate entry")){
                throw new GestorException("Paciente/DNI duplicado");
            }
            throw new GestorException("No se ha podido insertar el paciente en la BD.");
        } catch (DBManager2Exception e) {
            LOG.debug("Controlada una DBManager2Exception al dar de alta un paciente." , e);
            throw new GestorException("Ha habido un problema con la conexión a la base de datos.");
        }
    }

    /**
     *
     * @param mod: el paciente que pasamos como parametro para modificar
     */
    public void modificarPaciente(Paciente mod)throws GestorException, SQLException,DBManager2Exception{
        String error ="";
        if(mod == null){
            throw new GestorException("Paciente vacio");
        }
        if(mod.getIdSeg() == null || mod.getIdSeg().isEmpty()){
            error += "Error de DNI. ";
        }
        if(mod.getNombre() == null || mod.getNombre().isEmpty())
            error+="Error de nombre. ";

        if(mod.getApellidos() == null || mod.getApellidos().isEmpty()){
            error+="Error de apellidos. ";
        }
        if(mod.getAseguradora() == null || mod.getAseguradora().isEmpty())
            error+="Error de aseguradora. ";

        if(!error.isEmpty()) {
            throw new GestorException(error);
        }
        Paciente buscado;
        try (Connection conex = DBManager2.getConn()){
            buscado =getPacienteFromId(mod.getId());
            String SQL = "UPDATE Paciente SET Pac_Nombre=?, Pac_Apellidos=?, Pac_SegMedico=?, Pac_IDSEG=? WHERE Pac_Id=?";
            LOG.trace("SQL1: " + SQL);

            PreparedStatement pstm = conex.prepareStatement(SQL);
            pstm.setInt(5,buscado.getId() );
            pstm.setString(1, mod.getNombre());
            pstm.setString(2, mod.getApellidos());
            pstm.setString(3, mod.getAseguradora());
            pstm.setString(4, mod.getIdSeg());
            int aux= pstm.executeUpdate();
            LOG.trace("Aux: {}", aux);
        } catch (SQLException e) {
            e.getStackTrace();
            throw e;
        } catch (DBManager2Exception e) {
            e.printStackTrace();
            throw e;
        }catch(GestorException e){
            throw new GestorException(e.getMessage());
        }

    }

    public List<Paciente> getPaciente(){
        Paciente resultado;
        ArrayList<Paciente> listado = new ArrayList<>();
        try (Connection conex = DBManager2.getConn()){
            String SQL = "SELECT Pac_Nombre, Pac_Apellidos, Pac_SegMedico,Pac_IDSEG, Pac_Id FROM Paciente ORDER BY Pac_Id";
            LOG.trace("SQL: " + SQL);

            PreparedStatement pstm = conex.prepareStatement(SQL);
            ResultSet rs = pstm.executeQuery();
            while(rs.next()){
                String nombre = rs.getString("Pac_Nombre");
                String apellidos = rs.getString("Pac_Apellidos");
                String seguro = rs.getString("Pac_SegMedico");
                String idSeg= rs.getString("Pac_IDSEG");
                int id = rs.getInt("Pac_Id");

                resultado = new PacienteBuilder()
                        .setApellidos(apellidos)
                        .setAseguradora(seguro)
                        .setIdSeg(idSeg)
                        .setNombre(nombre)
                        .setId(id)
                        .build();
                listado.add(resultado);
            }
        } catch (SQLException e) {
            LOG.debug("Controlada una SQLException." , e);
        } catch (DBManager2Exception e) {
            LOG.debug("Controlada una DBManager2Exception." , e);
        }
        return listado;
    }

    public Paciente getPacienteFromIDSEG (String idSeg) throws GestorException{

        Paciente resultado = null;

        try (Connection conex = DBManager2.getConn()){
            String SQL = "SELECT Pac_Nombre, Pac_Apellidos, Pac_SegMedico, Pac_Id FROM Paciente WHERE Pac_IDSEG LIKE ?";

            PreparedStatement pstm = conex.prepareStatement(SQL);
            pstm.setString(1, idSeg);

            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                resultado = new PacienteBuilder()
                        .setApellidos(rs.getString("Pac_Apellidos"))
                        .setAseguradora(rs.getString("Pac_SegMedico"))
                        .setIdSeg(idSeg)
                        .setNombre(rs.getString("Pac_Nombre"))
                        .setId(rs.getInt("Pac_Id"))
                        .build();
            } else {
                LOG.debug("No se ha encontrado ningún paciente con ID_SEG: {}.", idSeg);
            }

            if (rs.next()) {
                LOG.debug("Se buscaba un paciente por ID_SEG y se ha encontrado más de uno.");
            }

        } catch (SQLException e) {
            LOG.debug("Controlada una SQLException." , e);
            throw new GestorException("Algo ha ido mal con la base de datos al pedir un paciente por DNI.");
        } catch (DBManager2Exception e) {
            LOG.debug("Controlada una DBManager2Exception." , e);
            throw new GestorException("Algo ha ido mal con las conexiones a la base de datos al pedir un paciente por DNI.");
        }

        return resultado;
    }

    public Paciente getPacienteFromId (int id) throws GestorException {

        Paciente resultado = null;

        try (Connection conex = DBManager2.getConn()){
            String SQL = "SELECT Pac_Nombre, Pac_Apellidos, Pac_SegMedico, Pac_IDSEG FROM Paciente WHERE Paciente.Pac_Id LIKE ?";
            LOG.trace("SQL_getPacienteFromId: " + SQL);

            PreparedStatement pstm = conex.prepareStatement(SQL);
            pstm.setInt(1, id);

            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {

                resultado = new PacienteBuilder()
                        .setApellidos(rs.getString("Pac_Apellidos"))
                        .setAseguradora(rs.getString("Pac_SegMedico"))
                        .setIdSeg(rs.getString("Pac_IDSEG"))
                        .setNombre(rs.getString("Pac_Nombre"))
                        .setId(id)
                        .build();
            } else {
                LOG.debug("No se ha encontrado ningún paciente con id: {}.", id);
            }

            if (rs.next()) {
                LOG.debug("Se buscaba un paciente por ID y se ha encontrado más de uno.");
            }

        } catch (SQLException e) {
            LOG.debug("Controlada una SQLException." , e);
            throw new GestorException("Algo ha ido mal con la base de datos al pedir un paciente por ID.");
        } catch (DBManager2Exception e) {
            LOG.debug("Controlada una DBManager2Exception." , e);
            throw new GestorException("Algo ha ido mal con las conexiones a la base de datos al pedir un paciente por ID.");
        }

        return resultado;
    }

    @SuppressWarnings("unused")
    public List<Paciente> getPacientes() throws GestorException {

        ArrayList<Paciente> resultado = new ArrayList<>();

        try (Connection conex = DBManager2.getConn()){
            PreparedStatement ptstm = conex.prepareStatement(
                    "SELECT Pac_Id, Pac_IDSEG,Pac_Nombre, Pac_Apellidos, Pac_SegMedico" + //BBDD
                            " FROM Paciente " + //BBDD
                            "ORDER BY Pac_Apellidos ASC");//BBDD

            ResultSet rs = ptstm.executeQuery();

            while(rs.next()) {
                int id = rs.getInt("Pac_Id");//BBDD
                String idSeg = rs.getString("Pac_IDSEG");//BBDD
                String nombre = rs.getString("Pac_Nombre");//BBDD
                String apellidos = rs.getString("Pac_Apellidos");
                String segMedico = rs.getString("Pac_segMedico");//BBDD

                resultado.add(new PacienteBuilder()
                        .setId(id)
                        .setIdSeg(idSeg)
                        .setNombre(nombre)
                        .setApellidos(apellidos)
                        .setAseguradora(segMedico)
                        .build());
            }

        } catch (SQLException e) {
            LOG.debug("Controlada una SQLException." , e);
            throw new GestorException("Algo ha ido mal con la base de datos al listar los pacientes.");
        } catch (DBManager2Exception e) {
            LOG.debug("Controlada una DBManager2Exception." , e);
            throw new GestorException("Algo ha ido mal con las conexiones a la base de datos al listar los pacientes.");
        }

        return resultado;
    }

   /**
     * Método que nos cuenta los pacientes que hay en la base de datos.
     * @return El número de pacientes. Si hay error, devuelve -1.
     */
   @SuppressWarnings("unused")
   public int getNumeroPacientes() {

        int resultado = -1;
        String SQL = "SELECT count(Pac_Id) FROM Paciente";

        try (Connection conex = DBManager2.getConn()) {
            PreparedStatement ptstm = conex.prepareStatement(SQL);

            ResultSet rs = ptstm.executeQuery();

            if (rs.next()) {
                resultado = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOG.debug("Controlada una SQLException contando paciente (getNumeroPacientes()).", e);
        } catch (DBManager2Exception e) {
            LOG.debug("Controlada una DBManager2Exception  contando medicos (getNumeroPacientes()).", e);
        }

        return resultado;
   }
}
