package pl.ds.bimserver.impl;

import pl.ds.bimserver.deserializer.IfcStepDeserializer;

interface IfcStepDeserializersProvider {

    IfcStepDeserializer getIfc2x3tc1StepDeserializer();

    IfcStepDeserializer getIfc4StepDeserializer();
}
