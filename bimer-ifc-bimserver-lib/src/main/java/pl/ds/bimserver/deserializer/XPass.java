package pl.ds.bimserver.deserializer;

/******************************************************************************
 * Copyright (C) 2009-2019  BIMserver.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import com.google.common.base.Charsets;
import org.bimserver.ifc.step.deserializer.Pass;
import org.bimserver.plugins.deserializers.DeserializeException;

/*
This and related classes (IfcParserWriterUtils, ParserPlan, XPass) are copied from opensourceBIM/IfcPlugins repository and can be removed
when this PR:
https://github.com/opensourceBIM/IfcPlugins/pull/15
is released (probably in ifcplugins-0.0.100).
*/

public class XPass extends Pass {

    @Override
    public String process(long lineNumber, String result) throws DeserializeException {
        while (result.contains("\\X\\")) {
            result = result.replace("\\\\", "\\");
            int index = result.indexOf("\\X\\");
            int code = Integer.parseInt(result.substring(index + 3, index + 5), 16);
            ByteBuffer b = ByteBuffer.wrap(new byte[] { (byte) (code) });
            CharBuffer decode = Charsets.ISO_8859_1.decode(b);
            result = result.substring(0, index) + decode.get() + result.substring(index + 5);
        }
        return result;
    }
}
