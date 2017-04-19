/*
 * NMRFx Processor : A Program for Processing NMR Data 
 * Copyright (C) 2004-2017 One Moon Scientific, Inc., Westfield, N.J., USA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nmrfx.processor.math.units;

import org.nmrfx.processor.math.Vec;

/**
 *
 * @author johnsonb
 */
public abstract class Unit<T extends java.lang.Number> extends java.lang.Number {

    T field;

    @Override
    public int intValue() {
        return field.intValue();
    }

    @Override
    public long longValue() {
        return field.longValue();
    }

    @Override
    public float floatValue() {
        return field.floatValue();
    }

    @Override
    public double doubleValue() {
        return field.doubleValue();
    }

    public abstract double getDoublePosition(Vec vec);

}
