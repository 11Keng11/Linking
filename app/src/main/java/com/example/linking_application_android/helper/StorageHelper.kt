package com.example.linking_application_android.helper


import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
//import com.google.gson.GsonBuilder
import java.io.*

class StorageHelper {

    data class Packet(var n: Int, var d: Array<Double>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Packet

            if (n != other.n) return false
            if (!d.contentEquals(other.d)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = n
            result = 31 * result + d.contentHashCode()
            return result
        }
    }

    companion object {

        fun cleanListStringToString(item: List<String>): String {
//        println("size of msg: ${item.size}")//16
            var str = ""
            for (m in item){
                val list_m = m.split(",")
//            println("item: $list_m")
                for (n in list_m){
//                println("item: $n")
                    if (n.isNotEmpty()) {
                        val s = n.slice(2..3)
                        str += s
//                    println("item: $s")
                    }
                }
            }
//        println("str size: ${str.length}")
            return str
        }

        fun getResUri(c: Context, location: Int): Uri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    c.resources.getResourcePackageName(location) + '/' +
                    c.resources.getResourceTypeName(location) + '/' +
                    c.resources.getResourceEntryName(location)
        )

        fun getCSVFromUri(context: Context, uri: Uri?): List<String> {
            val stream = uri?.let { context.contentResolver.openInputStream(it) }
            val bufferedReader = stream!!.bufferedReader()
            val listString = bufferedReader.readLines()
            bufferedReader.close()
            return listString
        }

        fun readCSVFromPath(p: String?): List<String> {
            //TODO to complete this function
            val stream =  File(p!!)
            val bufferedReader = stream.bufferedReader()
            val listString = bufferedReader.readLines()
            bufferedReader.close()
            return listString
        }

        fun readCSVFromUri(context: Context, uri:Uri?, startrow: Int?,endrow: Int?, startcol: Int?, endcol: Int?)
                : Array<Array<Double>> {
            //Open the file and read the contents
            val text = getCSVFromUri(context, uri)
            return readCSV(text, startrow, endrow, startcol, endcol)
        }

//        fun readCSVFromPath(path:String?, startrow: Int?,endrow: Int?, startcol: Int?, endcol: Int?)
//                : Array<Array<Double>> {
//            //Open the file and read the contents
//            val text = readFileFromPath(path)
//            return readCSV(text, startrow, endrow, startcol, endcol)
//        }

        fun readCSV(text: List<String>, startrow: Int?,endrow: Int?, startcol: Int?, endcol: Int?): Array<Array<Double>> {

            // Ensure the parameters are in correct values
            require(startrow != null && startrow >= 0)
            require(startcol != null && startcol >= 0)
            if (endrow != null) { require(endrow >= 0) }
            if (endcol != null) { require(endcol >= 0) }

            // Select which part of the csv for relevant data
            val lengthrows = text.size.minus(1)
            val lengthcols = text[startrow].split(",").size.minus(1)
            var packet_data = arrayOf<Array<Double>>()
            val srow = startrow
            var erow = endrow
            val scol = startcol
            var ecol = endcol

            // if end row and end column targeted are null
            // the full length of the row or/and column be read
            if (endrow == null) { erow = lengthrows }
            if (endcol == null) { ecol = lengthcols }

            /*
             *  Loop through the rows targeted of the csv file
             */
            for (x in srow..erow!!) {
                val t = text[x].split(",")
                var d =  arrayOf<Double>() // arrayOf<Double>(t[0].toDouble())//
                /*
                *  Loop through the columns targeted of the csv file
                */
                for (r in scol..ecol!!) {
                    // append the data to the array variable
                    d += t[r].toDouble()
                }
                // append the data from column into row array variable
                packet_data += d
            }
            return packet_data
        }

        fun writeCSV(c: Context, storage: String = "INTERNAL", fileName:String, header:String, data: Array<Array<Any>>?, append: Boolean = false): Boolean {
            var yourFilePath = ""
            when (storage){
                "INTERNAL" ->  yourFilePath = c.filesDir.toString() + "/" + fileName + ".csv"
                "EXTERNAL" ->  yourFilePath = c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + fileName + ".csv"
            }

            println("debug: $yourFilePath")

            lateinit var fileWriter: FileWriter
            try {

                fileWriter = FileWriter(yourFilePath, append)
                if (header != ""){
                    fileWriter.append(header)
                    fileWriter.append('\n')
                }
                if (data!=null) {
                    println(data.contentDeepToString())
                    data.forEach {
                        it.forEach { it1 ->
                            fileWriter.append(it1.toString())
                            fileWriter.append(',')
                        }
                        fileWriter.append('\n')
                    }
                }

                println("debug: Write CSV successfully!")
                return true
            } catch (e: Exception) {
                println("debug: Writing CSV error!")
                e.printStackTrace()
            } finally {
                try {
                    fileWriter.flush()
                    fileWriter.close()
                } catch (e: IOException) {
                    println("debug: Flushing/closing error!")
                    e.printStackTrace()
                }
            }
            return false
        }

//        fun writeFileInJSON(context: Context, data: Any, fileName: String) {
//            /*
//                Write and read into internal in JSON format
//             */
//            val gsonPretty = GsonBuilder().setPrettyPrinting().create()
//            val jsonTutsListPretty: String = gsonPretty.toJson(data)
//
//            //Get your FilePath and use it to create your File
//            val yourFilePath = context.filesDir.toString() + "/" + fileName + ".json"
//            val yourFile = File(yourFilePath)
//
//            //Create your FileOutputStream, yourFile is part of the constructor
//            val fileOutputStream = FileOutputStream(yourFile)
//
//            //Convert your JSON String to Bytes and write() it
//            fileOutputStream.write(jsonTutsListPretty.toByteArray())
//
//            //Finally flush and close your FileOutputStream
//            fileOutputStream.flush()
//            fileOutputStream.close()
//        }
//
//        fun readFileFromJSON(context: Context, fileName: String): List<Any> {
//            //Get your FilePath and use it to create your File
//            val yourFilePath = context.filesDir.toString() + "/" + fileName + ".json"
//
//            // Check if the file exist
//            val yourFileExist = File(yourFilePath).exists()
//            var text: String = ""
//
//            // if File exist read the file
//            if (yourFileExist) {
//                val yourFile = File(yourFilePath)
//                //Create your FileOutputStream, yourFile is part of the constructor
//                val fileInputStream = FileInputStream(yourFile)
//                //Convert your JSON String to Bytes and write() it
//                text = fileInputStream.bufferedReader().readText()
//                fileInputStream.close()
//            }
//            //else return the blank text and the boolean result
//            return listOf(text, yourFileExist)
//        }

        fun deleteAllFile(context: Context) {
            /*
             *  Delete all the file in the internal storage data/user/
             */
            val yourFileList = context.filesDir.list()
            yourFileList!!.forEach {
                println("File: $it")
                File(context.filesDir.toString() + "/" + it.toString()).delete()
            }
        }

        // Checks if a volume containing external storage is available
        // for read and write.
        fun isExternalStorageWritable(): Boolean {
            return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        }

        // Checks if a volume containing external storage is available to at least read.
        fun isExternalStorageReadable(): Boolean {
            return Environment.getExternalStorageState() in
                    setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
        }

        fun getFilesFromExternalStorageDownloads(c: Context, fileName:String)
                = c.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + fileName)

        fun getImagesFromExternalStorageFiles(f:File?): List<File?> {
            val fList = mutableListOf<File?>()
            f?.listFiles()?.forEach { fList.add(it) }
            return fList
        }

    }
}
