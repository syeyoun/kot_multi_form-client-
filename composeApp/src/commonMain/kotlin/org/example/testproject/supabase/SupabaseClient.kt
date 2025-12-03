package org.example.testproject.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
//import io.github.jan.supabase.serializer.KotlinxSerializer
import kotlinx.serialization.json.Json

object SupabaseClient {
    val json = Json{
        ignoreUnknownKeys = true
    }
    val client = createSupabaseClient(
        supabaseUrl = "https://bmfooaubafaqdfefmsrv.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJtZm9vYXViYWZhcWRmZWZtc3J2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjExOTc1ODIsImV4cCI6MjA3Njc3MzU4Mn0.KenmDD0TVIcaqUPiE9EyCte0GgsEyJJfeqUdtsAhS_Q"
    ) {
        install(Postgrest)
        install(Realtime)

//        defaultSerializer = KotlinxSerializer(
//            val json {
//                ignoreUnknownKeys = true
//            }
//        )
    }
}