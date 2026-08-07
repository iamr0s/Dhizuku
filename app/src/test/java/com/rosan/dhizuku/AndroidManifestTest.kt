package com.rosan.dhizuku

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class AndroidManifestTest {
    @Test
    fun dhizukuDeviceAdminReceiverMaintainsActivationContract() {
        val androidNamespace = "http://schemas.android.com/apk/res/android"
        val manifest = File("src/main/AndroidManifest.xml")
        val document = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(manifest)

        val receiver = (0 until document.getElementsByTagName("receiver").length)
            .map { document.getElementsByTagName("receiver").item(it) as Element }
            .firstOrNull {
                it.attributes.getNamedItemNS(androidNamespace, "name")?.nodeValue ==
                    ".server.DhizukuDAReceiver"
            } ?: throw AssertionError("DhizukuDAReceiver must be declared")

        assertEquals(
            "true",
            receiver.attributes.getNamedItemNS(androidNamespace, "exported")?.nodeValue
        )
        assertEquals(
            "android.permission.BIND_DEVICE_ADMIN",
            receiver.attributes.getNamedItemNS(androidNamespace, "permission")?.nodeValue
        )

        val metadata = (0 until receiver.childNodes.length)
            .map { receiver.childNodes.item(it) }
            .firstOrNull {
                it.nodeName == "meta-data" &&
                    it.attributes.getNamedItemNS(androidNamespace, "name")?.nodeValue ==
                    "android.app.device_admin"
            } ?: throw AssertionError(
                "DhizukuDAReceiver must declare device-admin metadata"
            )

        assertEquals(
            "@xml/dhizuku_device_admin",
            metadata.attributes.getNamedItemNS(androidNamespace, "resource")?.nodeValue
        )

        val actions = (0 until receiver.getElementsByTagName("action").length)
            .map { receiver.getElementsByTagName("action").item(it) }
            .mapNotNull {
                it.attributes.getNamedItemNS(androidNamespace, "name")?.nodeValue
            }

        assertTrue(
            "DhizukuDAReceiver must advertise DEVICE_ADMIN_ENABLED",
            "android.app.action.DEVICE_ADMIN_ENABLED" in actions
        )
    }
}
