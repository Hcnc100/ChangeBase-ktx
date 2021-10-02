package core

import changeBase.SubsStringFormatBase
import java.math.BigDecimal
import java.math.BigInteger

/**
 * this class contains a group of utility's
 * that convert one number in any base an any other base,
 * */
object ChangeBase {


    /**
     * the valid function the string passed as an argument
     * has the valid characters for the corresponding base,
     * for that it makes use of regex expressions
     *  @param stringToValidate the string to validate
     *  @param base base to validate the string
     *  @return boolean that indicates if the string is valid
     * */
    fun validate(stringToValidate: String, base: Int): Boolean {
        //we obtain value to base in number or character
        val baseValue = (base - 1).toValueHex()
        // we obtain the expression regex consider the base
        val regex = when (base) {
            // if the base is less than 10,the characters contained
            // in range [0-9] are considered depending the base
            in 2..10 -> "[+-]?[0-$baseValue]+\\.?[0-$baseValue]*"
            // if the base is greater than 10, considered characters
            // alphanumeric , such as A->10,B-11 .. F->16
            in 11..16 -> "[+-]?[0-9A-$baseValue]+\\.?[0-9A-$baseValue]*"
            // if the base is not in this ranges, return auto false
            else -> return false

        }
        // and return the evaluation of this expresion
        val expRegex = Regex(regex)
        return expRegex.matches(stringToValidate)
    }

    /**
     * the function divide and format the string passed to argument,
     * it need validate, if not the comportment is indefinite
     *  @return SubsStringFormatBase this, contains the string split
     *  @see ChangeBase.validate
     *  @see SubsStringFormatBase
     * */

    private fun preformatString(numberToFormat: String): SubsStringFormatBase {
        // and the location of the number sign, if it contains
        //use regex

        //separate the number of the sing
        val (numberWithOutSigned, sign) =
            if (numberToFormat.contains(Regex("[+-]"))) {
                Pair(
                    numberToFormat.substring(1, numberToFormat.length),
                    numberToFormat[0].toString()
                )
            } else {
                Pair(numberToFormat, "")
            }

        // we obtain the location of point, this is important
        val indexPoint = numberWithOutSigned.indexOf('.')

        //determine if the number has only part integer or is fractional
        // and divide that parts
        val (hasFractional, partInt, partFloat) = if (indexPoint != -1) {
            Triple(
                true,
                numberWithOutSigned.substring(0 until indexPoint),
                numberWithOutSigned.substring(indexPoint + 1, numberWithOutSigned.length)
            )
        } else {
            Triple(
                false,
                numberWithOutSigned,
                ""
            )
        }
        //return the numbers in parts
        return SubsStringFormatBase(
            hasFractional, sign, partInt, partFloat
        )
    }

    /**
     *  main function that convert any number from any base, to anywhere
     *
     *  @param numberString number to convert
     *  @param baseFrom base from number
     *  @param baseTo base to convert
     *  @return string that contains the number convert
     * */

    fun baseToBase(numberString: String, baseFrom: Int, baseTo: Int): String {
        if (!validate(numberString, baseFrom)) return ""
        return if (baseFrom != baseTo) {
            with(preformatString(numberString)) {
                val finalPartInt = changeBasePartInteger(baseFrom, baseTo, partInteger)
                val finalPartFloat = if (hasPartFractional) {
                    "."+changeBasePartFractional(baseFrom, baseTo, partFractional)
                } else {
                    ".0"
                }
                "$signed$finalPartInt$finalPartFloat"
            }
        } else {
            numberString
        }
    }

    /**
     * the function change the base of number passed as argument in another base,
     * the algorithm indicates that:
     *
     * 1.- Pass the number from any base to number base 10
     *
     * 2.- Pass the number convert in another base
     *
     * So, if the base of the number is one init 10, cant jump this step,
     * and if the base a convert is base 10, also cant jump this step,
     * this only save calculations
     * */

    private fun changeBasePartFractional(baseFrom: Int, baseTo: Int, numberString: String): String {
        val numberFractionalBase10 = if (baseFrom != 10) {
            anyPartFloatBaseToDecimal(
                numberString,
                baseFrom
            )
        } else {
            numberString
        }
        val numberFractionalAnyBase = if (baseTo != 10) {
            decimalPartFloatToAnyBase(numberFractionalBase10, baseTo)
        } else {
            numberFractionalBase10
        }
        return numberFractionalAnyBase
    }

    /**
     * the function change the base of number passed as argument in another base,
     * the algorithm indicates that:
     *
     * 1.- Pass the number from any base to number base 10
     *
     * 2.- Pass the number convert in another base
     *
     * So, if the base of the number is one init 10, cant jump this step,
     * and if the base a convert is base 10, also cant jump this step,
     * this only save calculations
     * */

    private fun changeBasePartInteger(baseFrom: Int, baseTo: Int, numberString: String): String {
        // we obtain the number in base 10 only if the base of the number passed
        // by argument different from base 10
        // this to avoid unnecessary calculations
        val numberIntegerBase10 = if (baseFrom != 10) {
            anyBasePartIntBaseToDecimal(
                numberString,
                baseFrom
            )
        } else {
            numberString
        }
        // now, if the base to convert is 10, doing the calculus
        // if not, return this number, given that this numbers is ready in base 10
        val numberIntegerAnyBase = if (baseTo != 10) {
            decimalPartIntToAnyBase(numberIntegerBase10, baseTo)
        } else {
            numberIntegerBase10
        }
        return numberIntegerAnyBase
    }

    /**
     * Converts integer number in anywhere base passed as argument,
     * this function make use "BigInteger" for more precision.
     * Make use string buffer to fast append strings
     *
     * @param numberDecimal has the number to convert
     * @param baseTo numeric base that use for convert number
     * @return string - number converted
     *
     * @see StringBuffer
     * @see BigInteger
     * */
    private fun decimalPartIntToAnyBase(numberDecimal: String, baseTo: Int): String {
        //variable that contains the current number, this save as bigInteger
        var currentNumber = BigInteger(numberDecimal)
        //variable that is saving the current part of response in every iteration
        val response = StringBuffer("")
        //variable that save the residue of the division between the current number and the base
        var residue: BigInteger
        //variable that save the representation of the base to convert in bigInteger
        val bigBase = BigInteger(baseTo.toString())
        //the loop will repeat while the current number is greater than zero
        while (currentNumber > BigInteger.ZERO) {
            //calculate the result of mod between the current number and the base
            residue = currentNumber % bigBase
            //obtains the value of this residue and append to answer
            response.append(residue.toInt().toValueHex())
            //the new current number will the result of the division between current
            // number and the base,only integer part
            currentNumber /= bigBase
        }
        //the answer is correct,only it is backwards
        return response.reverse().toString()
    }

    /**
     * Converts fractional part of base 10 number, in a fractional
     * number with the base passed as argument, this function make use
     * of BigDecimal for more precision.
     * If the number is periodic, will add '...'
     *
     * Make use string buffer to fast append strings
     *
     * @param numberFloat number to convert
     * @param baseTo base to convert the number
     * @return string - number converted in this base
     *
     * @see BigDecimal
     * @see StringBuffer
     * */

    private fun decimalPartFloatToAnyBase(numberFloat: String, baseTo: Int): String {
        //transform number a bigDecimal and is will be the first current number
        var currentNumber = BigDecimal("0.$numberFloat")
        //variable that has the response
        val response = StringBuffer("")
        // variable that has the base as bigDecimal
        val bigBase = BigDecimal(baseTo.toString())
        //variable that contains number part integer and part fractional
        var currentFullNumber: BigDecimal
        //variable that avoid loops
        var lastNumber = BigDecimal.ZERO
        //the loops always repeat if the current number is grater than 0
        while (currentNumber > BigDecimal.ZERO) {
            // multiply the current number for the base
            currentFullNumber = currentNumber * bigBase
            //if the last calculate number is equal this, so break, because
            //if not entry in a loop infinity
            if (lastNumber == currentFullNumber) {
                //and add ... to indicate periodic number
                response.append("...")
                break
            }
            //if not is equals save this number
            //this will the last calculate number
            lastNumber = currentFullNumber
            // obtains the integer part
            val numberPartInt = currentFullNumber.toInt()
            //obtains to representation in base hex
            // since is contains all digits and letter valid
            response.append(numberPartInt.toValueHex())
            //the new current number will, the full number minus
            //the fractional part
            currentNumber = currentFullNumber - BigDecimal(numberPartInt)
        }
        return response.toString()
    }

    /**
     * Converts any integer number to base 10 number, make use
     * BigInteger for more precision
     *
     * @param numberString number to convert
     * @param base base to change number
     * @return string - this contains the convert number
     * */

    private fun anyBasePartIntBaseToDecimal(numberString: String, base: Int): String {
        //variable that save the base as BigInteger
        val baseBig = BigInteger(base.toString())
        //variable that contains the current value of the convert
        var currentValue = BigInteger.ZERO
        //var that contains the current base
        var currentPower = BigInteger.ONE
        //this loop start in the right side of the string
        for (i in numberString.length - 1 downTo 0) {
            //obtains the integer value to the current char
            val digit = BigInteger(numberString[i].valueHexToDec().toString())
            //the current value will be this digit multiply for the current power
            currentValue += digit * currentPower
            //multiply the power by the base, this for increase the power by one
            currentPower *= baseBig
        }
        //after return this current value as string
        return currentValue.toString()
    }

    /**
     * Converts any fractional number to base 10 number, make use
     * BigDecimal for more precision
     *
     * @param numberString number to convert
     * @param base base to change number
     * @return string - this contains the convert number
     * */

    private fun anyPartFloatBaseToDecimal(numberString: String, base: Int): String {
        //variable that save the current value
        var value = BigDecimal.ZERO
        //variable that save the current power
        var currentPower = BigDecimal(base.toString())
        //variable that save the representation of the base as bigDecimal
        val bigBase = BigDecimal(base.toString())
        for (index in numberString.indices) {
            //save the char value as BigDecimal
            val digit = BigDecimal(numberString[index].valueHexToDec())
            //add to the current value the value of the digit multiplied by one over the current base
            val fractional=BigDecimal.ONE.divide(currentPower)
            value += digit * fractional
            //multiply the power by the base, this for increase the power by one
            currentPower *= bigBase
        }
        return value.toString().removePrefix("0.")
    }

    /**
     * Simple function that convert one value int
     * to value alphanumeric scale
     * */

    private fun Int.toValueHex(): String =
        when (this) {
            in 0..9 -> this
            in 10..15 -> ('A'.code + this - 10).toChar()
            else -> -1
        }.toString()

    /**
     * Simple function that convert one value char in your
     * value decimal
     * */

    private fun Char.valueHexToDec(): Int =
        when (this) {
            in '0'..'9' -> this.code - '0'.code
            in 'A'..'F' -> this.code - 'A'.code + 10
            else -> -1
        }


}