package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AeroGuide", appName)
  }

  @Test
  fun `verify theme persistence in AeroStorage`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = com.example.data.storage.AeroStorage(context)
    
    storage.setDarkTheme(false)
    assertEquals(false, storage.isDarkTheme())

    storage.setDarkTheme(true)
    assertEquals(true, storage.isDarkTheme())
  }

  @Test
  fun `verify login session persistence in AeroStorage`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = com.example.data.storage.AeroStorage(context)

    storage.setLoggedIn(true, "alex.passenger@example.com", "Alex Passenger", "usr_alex")
    assertEquals(true, storage.isLoggedIn())
    assertEquals("alex.passenger@example.com", storage.getUserEmail())
    assertEquals("usr_alex", storage.getUserUid())

    storage.clearSession()
    assertEquals(false, storage.isLoggedIn())
  }
}
