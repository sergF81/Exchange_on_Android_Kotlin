package com.example.kotlinmanyexchange

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList
import java.util.concurrent.ExecutionException
import java.util.regex.Pattern

import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

    //создаем переменную, в которой будут храниться ссобщения о результатах обмена
    var infoMessage: String = ""

    //переменная с адресом сайта, с которого берем данные по курсам валют
    var valueResourceHTTPS = "https://www.cbr-xml-daily.ru/daily_json.js"

    //переменная, в которой хранится информация о валюте, которую нужно обменять
    var manyStartValue: String = ""

    //переменная, в которой хранится информация о валюте, на которую нужно обменять
    var manyEndValue: String = ""

    //переменная, в которой хранится информация с результатом обмена
    private var resultExchange: String = ""

    //массив, в который будет заноситься информация по выполненым транзакциям
    var log = ArrayList<String>()

    //переменная с исходным значеним рубля
    var rub = 1

    //переменные, в которых будут хранится данные о екрсах валют
    var dollar: Double = 0.00
    var euro: Double = 0.00


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var textViewInfo = findViewById<TextView>(R.id.textViewInfo)
        var textViewInfoKurs = findViewById<TextView>(R.id.textViewInfoKurs)
        var editTextInsertValue = findViewById<EditText>(R.id.editTextInsertValue)
        var buttonOK = findViewById<Button>(R.id.buttonOK)
        var buttonRub = findViewById<Button>(R.id.buttonRub)
        var buttonDollar = findViewById<Button>(R.id.buttonDollar)
        var buttonEuro = findViewById<Button>(R.id.buttonEuro)
        var buttonLog = findViewById<Button>(R.id.buttonLog)

        editTextInsertValue.visibility = View.INVISIBLE
        editTextInsertValue.setText("")
        buttonOK?.visibility = View.INVISIBLE
        valueMany()
        textViewInfoKurs.text = " Курс валют: Доллар - $dollar, Евро - $euro."
    }

    //обработка нажатия кнопки выбора валюты Рубль
    fun onClickRub(view: View) {

        if (manyStartValue != "") {
            manyEndValue = "rub"
            if (manyStartValue === manyEndValue) {
                Toast.makeText(
                    this@MainActivity,
                    "Нельзя менять Рубль на Рубль!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                ExchangeValue()
                disableAllButtons()
            }

        } else {
            buttonOK.visibility = View.VISIBLE
            editTextInsertValue.visibility = View.VISIBLE
            manyStartValue = "rub"
            textViewInfo.text = "Введите количество валюты для обмена."
            disableAllButtons()
            buttonOK.visibility = View.VISIBLE
        }


    }

    //обработка нажатия кнопки выбора валюты Доллар
    fun onClickDollar(view: View) {
        if (manyStartValue != "") {
            manyEndValue = "dollar"
            if (manyStartValue === manyEndValue) {
                Toast.makeText(
                    this@MainActivity,
                    "Нельзя менять Доллар на Доллар!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                ExchangeValue()
                disableAllButtons()
            }
        } else {
            buttonOK.visibility = View.VISIBLE
            editTextInsertValue.visibility = View.VISIBLE
            manyStartValue = "dollar"
            textViewInfo.text = "Введите количество валюты для обмена."
            disableAllButtons()
            buttonOK.visibility = View.VISIBLE
        }


    }

    //обработка нажатия кнопки выбора валюты Евро
    fun onClickEuro(view: View) {
        //проверяем, если кнопка нажата первый раз и manyStartValue не равно "", тогда присваиваем значение выбранной валюты
        if (manyStartValue != "") {
            manyEndValue = "euro"
            //проверяем, выбрал ли пользователь при повторном нажатии одну и туже валюту для обмена
            if (manyStartValue === manyEndValue) {
                Toast.makeText(this@MainActivity, "Нельзя менять Евро на Евро!", Toast.LENGTH_SHORT)
                    .show()
                //если при повторно мнажатии кнокпи, значения manyStartValue не равно manyEndValue, тогда выполняем ExchangeValue() и отключаем видимость кнопок выбора валют (rub, dollar, euro)
            } else {
                ExchangeValue()
                disableAllButtons()

            }
            //если кнопка выбора валюты нажата в первый раз, тогда показвыаем окно ввода  и кнопку ОК для подтверждения ввода и отключаем видимость кнопок выбора валют (rub, dollar, euro)
            //и присваем значение manyStartValue о первом нажатии
        } else {
            buttonOK.visibility = View.VISIBLE
            editTextInsertValue.visibility = View.VISIBLE
            manyStartValue = "euro"
            textViewInfo.text = "Введите количество валюты для обмена."
            disableAllButtons()
            buttonOK.visibility = View.VISIBLE
        }
    }

    //обработка нажатия кнопки ОК для подтверждения ввода количества валюты для обмена
    fun onClickOK(view: View) {
        //переенная для проверки путого значения ввода
        val proverkaVvoda = editTextInsertValue.text.toString()

        //проверка на пустое значение
        if (proverkaVvoda == "") {
            Toast.makeText(
                this@MainActivity,
                "Вы не ввели количество валюты для обмена!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            textViewInfo.text = "Выберите валюту, которую хотите получить"
            enableAllButtons()
            editTextInsertValue.visibility = View.INVISIBLE
            buttonOK.visibility = View.INVISIBLE
        }

    }

    //функция вычисления количества полученной валюты
    fun ExchangeValue() {
        //переменная, которая содержит значение о введеном количестве валюты для обмена
        val num = Integer.parseInt(editTextInsertValue.text.toString())

        when (manyStartValue) {
            "rub" -> when (manyEndValue) {
                "rub" -> {
                    resultExchange = "0"
                    textViewInfo.text = "Вы получтли $resultExchange"
                }
                "dollar" -> {
                    resultExchange = String.format("%.2f", num / dollar as Double)
                    infoMessage = "Вы обменяли $num Руб  на $resultExchange $."
                    textViewInfo.text = infoMessage
                    // добавляем историю транзации в массив для вывода в новой активности для логов
                    log.add(infoMessage)
                }
                "euro" -> {
                    resultExchange = String.format("%.2f", num / euro as Double)
                    infoMessage = "Вы обменяли $num Руб  на $resultExchange €."
                    textViewInfo.text = infoMessage
                    log.add(infoMessage)
                }
            }

            "dollar" -> when (manyEndValue) {
                "rub" -> {
                    resultExchange = String.format("%.2f", num * dollar!!)
                    infoMessage = "Вы обменяли $num $ на $resultExchange Руб."
                    textViewInfo.text = infoMessage
                    log.add(infoMessage)
                }
                "dollar" -> {
                    resultExchange = "0"
                    textViewInfo.text = "Вы получтли $resultExchange"
                }
                "euro" -> {
                    resultExchange = String.format("%.2f", num * dollar!! / euro as Double)
                    infoMessage = "Вы обменяли $num $ на $resultExchange €."
                    textViewInfo.text = infoMessage
                    log.add(infoMessage)
                }
            }
            "euro" -> when (manyEndValue) {
                "rub" -> {
                    resultExchange = String.format("%.2f", num * euro as Double)
                    infoMessage = "Вы обменяли $num € на $resultExchange Руб."
                    textViewInfo.text = infoMessage
                    log.add(infoMessage)
                }
                "dollar" -> {
                    resultExchange = String.format("%.2f", num * euro!! / dollar as Double)
                    infoMessage = "Вы обменяли $num € на $resultExchange $."
                    textViewInfo.text = infoMessage
                    log.add(infoMessage)
                }
                "euro" -> {
                    resultExchange = "0"
                    textViewInfo.text = "Вы получтли $resultExchange"
                }
            }
        }
    }

    //функция для отключения 3-х кнопок выбора валюты
    fun disableAllButtons() {

        buttonRub.visibility = View.INVISIBLE
        buttonDollar.visibility = View.INVISIBLE
        buttonEuro.visibility = View.INVISIBLE
    }

    //функция для включения 3-х кнопок выбора валюты
    fun enableAllButtons() {
        buttonRub.visibility = View.VISIBLE
        buttonDollar.visibility = View.VISIBLE
        buttonEuro.visibility = View.VISIBLE
    }

    //обработка нажатия кнопки Restart для обнуления и запуска по новой процесса обмена валюты
    fun onClickRestart(view: View) {
        enableAllButtons()
        buttonOK.visibility = View.INVISIBLE
        editTextInsertValue.visibility = View.INVISIBLE
        textViewInfo.text = "Выберите валюту, которую хотите обменять"
        manyEndValue = ""
        manyStartValue = ""
        editTextInsertValue.setText("")
        editTextInsertValue.hint = "0"


    }

    //обработка нажатия кнопки HISTORY(Log) для передачи данным массива с данными о проделанных операциях обмена валюты в другую активность
    fun onClickLog(view: View) {
        //создание интената для новой активности
        val intent = Intent(this@MainActivity, LogActivity::class.java)
        //передача массива в другую активность
        intent.putStringArrayListExtra("log", log)
        //запус активности
        startActivity(intent)
    }

    //создаем класс для подключения к сайту с данными по курсам валют
    class GetValueMany : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg strings: String): String? {

            var url: URL? = null
            var urlConnection: HttpsURLConnection? = null
            val result = StringBuilder()

            try {
                url = URL(strings[0])

                urlConnection = url.openConnection() as HttpsURLConnection


                val inputStream = urlConnection.inputStream

                val inputStreamReader = InputStreamReader(inputStream)
                val reader = BufferedReader(inputStreamReader)
                var line: String? = reader.readLine()
                while (line != null) {
                    result.append(line)
                    line = reader.readLine()
                }

                return result.toString()


            } catch (e: FileNotFoundException) {


            } catch (e: MalformedURLException) {

                e.printStackTrace()
            } catch (e: IOException) {

                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
            }
            urlConnection!!.disconnect()
            return null
        }


        override fun onPostExecute(s: String) {

            super.onPostExecute(s)

        }
    }

    //функция для вырезания нужных значения из данных с сайта о курсах валют
    private fun valueMany() {
        val task = GetValueMany()
        println("_____dollar $dollar")
        try {

            val taskgit = task.execute(valueResourceHTTPS).get()
            val start = "USD"
            val finish = "INR"

            val pattern = Pattern.compile("$start(.*?)$finish")
            val matcher = pattern.matcher(taskgit)
            var allValue: String? = ""
            while (matcher.find()) {
                allValue = matcher.group(1)

            }

            val pattern1 = Pattern.compile("Value\": (.*?),")
            val matcher1 = pattern1.matcher(allValue)
            var i = 0
            while (matcher1.find()) {
                i++
                if (i != 2) {
                    dollar = java.lang.Double.parseDouble(matcher1.group(1))
                } else {
                    euro = java.lang.Double.parseDouble(matcher1.group(1))
                }
            }


        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }


    }


}
