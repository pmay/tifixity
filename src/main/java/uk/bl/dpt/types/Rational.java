/**
 * Copyright 2016 Peter May
 * Author: Peter May
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.bl.dpt.types;

/**
 * Class capturing a Rational tuple
 */
public class Rational {
    private Integer numerator;
    private Integer denominator;

    public Rational(Integer numerator, Integer denominator){
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public Integer getNumerator(){
        return this.numerator;
    }

    public Integer getDenominator(){
        return this.denominator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rational rational = (Rational) o;

        if(numerator!=rational.numerator) return false;
        return (denominator==rational.denominator);
    }
}
