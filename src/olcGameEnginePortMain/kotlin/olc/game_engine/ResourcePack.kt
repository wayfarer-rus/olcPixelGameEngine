package olc.game_engine

import platform.posix.fclose
import platform.posix.fopen

/**

License (OLC-3)
~~~~~~~~~~~~~~~
Copyright 2018 - 2019 OneLoneCoder.com
Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:
1. Redistributions or derivations of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions or derivative works in binary form must reproduce the above
copyright notice. This list of conditions and the following	disclaimer must be
reproduced in the documentation and/or other materials provided with the distribution.
3. Neither the name of the copyright holder nor the names of its contributors may
be used to endorse or promote products derived from this software without specific
prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS	"AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT	HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL,	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

Links
~~~~~
YouTube:	https://www.youtube.com/javidx9
https://www.youtube.com/javidx9extra
Discord:	https://discord.gg/WhwHUMV
Twitter:	https://www.twitter.com/javidx9
Twitch:		https://www.twitch.tv/javidx9
GitHub:		https://www.github.com/onelonecoder
Homepage:	https://www.onelonecoder.com
Patreon:	https://www.patreon.com/javidx9
 */

interface ResourcePack {
    fun AddToPack(file: String): rcode
    fun SavePack(file: String): rcode
    fun LoadPack(file: String): rcode
    fun ClearPack(file: String): rcode
    fun GetStreamBuffer(file: String): ByteArray?
}

class ResourcePackImpl : ResourcePack {
    private var mapFiles: MutableMap<String, ByteArray> = HashMap()

    override fun GetStreamBuffer(file: String): ByteArray? {
        return mapFiles[file]
    }

    @ExperimentalUnsignedTypes
    override fun AddToPack(file: String): rcode {
        val fh = fopen(file, "r") ?: return rcode.FAIL

        try {
            mapFiles[file] = fh.fileToByteArray()
        } finally {
            fclose(fh)
        }

        return rcode.OK
    }

    override fun SavePack(file: String): rcode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun LoadPack(file: String): rcode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ClearPack(file: String): rcode {
        mapFiles.clear()
        return rcode.OK
    }

}