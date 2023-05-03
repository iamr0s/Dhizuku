package com.rosan.dhizuku.server

import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import com.rosan.dhizuku.shared.DhizukuVariables

class DhizukuUserServiceArgs {
    private var bundle = Bundle();

    var componentName: ComponentName
        get() = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            bundle.getParcelable(DhizukuVariables.PARAM_COMPONENT, ComponentName::class.java)
        else bundle.getParcelable(DhizukuVariables.PARAM_COMPONENT))!!
        set(value) = bundle.putParcelable(DhizukuVariables.PARAM_COMPONENT, value)

    constructor(bundle: Bundle) {
        this.bundle = bundle
    }

    constructor(args: DhizukuUserServiceArgs) : this(args.bundle)

    constructor(name: ComponentName) : this(Bundle().apply {
        putParcelable(DhizukuVariables.PARAM_COMPONENT, name)
    })

    fun build(): Bundle {
        return bundle;
    }
}