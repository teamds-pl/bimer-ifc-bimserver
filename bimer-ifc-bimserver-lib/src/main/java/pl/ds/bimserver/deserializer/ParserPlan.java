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

import java.util.ArrayList;
import java.util.List;

import org.bimserver.ifc.step.deserializer.Pass;
import org.bimserver.plugins.deserializers.DeserializeException;

/*
This and related classes (IfcParserWriterUtils, ParserPlan, XPass) are copied from opensourceBIM/IfcPlugins repository and can be removed
when this PR:
https://github.com/opensourceBIM/IfcPlugins/pull/15
is released (probably in ifcplugins-0.0.100).
*/

public class ParserPlan {
    private final List<Pass> passes = new ArrayList<>();

    public ParserPlan(Pass... passes) {
        for (Pass pass : passes) {
            this.passes.add(pass);
        }
    }

    public void add(Pass pass) {
        passes.add(pass);
    }

    public String process(long lineNumber, String input) throws DeserializeException {
        for (Pass pass : passes) {
            input = pass.process(lineNumber, input);
        }
        return input;
    }
}
