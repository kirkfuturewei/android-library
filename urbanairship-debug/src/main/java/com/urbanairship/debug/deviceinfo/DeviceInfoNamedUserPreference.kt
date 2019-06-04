/* Copyright Urban Airship and Contributors */

package com.urbanairship.debug.deviceinfo

import android.content.Context
import android.support.v7.preference.EditTextPreference
import android.util.AttributeSet
import com.urbanairship.UAirship
import com.urbanairship.util.UAStringUtil

/**
 * DialogPreference to set the named user.
 */
class DeviceInfoNamedUserPreference(context: Context, attrs: AttributeSet) : EditTextPreference(context, attrs) {

    override fun setText(text: String) {
        val namedUser = if (UAStringUtil.isEmpty(text)) null else text
        UAirship.shared().namedUser.id = namedUser
        notifyChanged()
    }

    override fun getText(): String? {
        return UAirship.shared().namedUser.id
    }

    override fun getSummary(): String? {
        return UAirship.shared().namedUser.id
    }

    override fun shouldPersist(): Boolean {
        return false
    }
}