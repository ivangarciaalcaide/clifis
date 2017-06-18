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

package es.upm.etsisi.clifis.model;

import java.io.Serializable;

public class Paciente implements Serializable {
    private String nombre;
    private String apellidos;
    // IdSeg: Identificador para la aseguradora (ej.: Nº de la Seg. Social).
    private String idSeg;
    private String aseguradora;
    private int id;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getIdSeg() {
        return this.idSeg;
    }

    public void setIdSeg(String idSeg) {
        this.idSeg = idSeg;
    }

    public String getAseguradora() {
        return aseguradora;
    }

    public void setAseguradora(String aseguradora) {
        this.aseguradora = aseguradora;
    }

    public Paciente(int id, String nombre, String apellidos, String idSeg, String aseguradora) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.idSeg = idSeg;
        this.aseguradora = aseguradora;
    }
}
