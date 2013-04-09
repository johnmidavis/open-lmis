/*
 * Copyright © 2013 VillageReach.  All Rights Reserved.  This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 *
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.openlmis.core.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.core.builder.SupplyLineBuilder;
import org.openlmis.core.domain.Program;
import org.openlmis.core.domain.SupervisoryNode;
import org.openlmis.core.domain.SupplyLine;
import org.openlmis.core.exception.DataException;
import org.openlmis.core.repository.FacilityRepository;
import org.openlmis.core.repository.ProgramRepository;
import org.openlmis.core.repository.SupervisoryNodeRepository;
import org.openlmis.core.repository.SupplyLineRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class SupplyLineServiceTest {

  @Mock
  private SupplyLineRepository supplyLineRepository;

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @InjectMocks
  private SupplyLineService supplyLineService;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  private SupplyLine supplyLine;

  @Before
  public void setUp() throws Exception {
    supplyLine = make(a(SupplyLineBuilder.defaultSupplyLine));
    supplyLineService = new SupplyLineService(supplyLineRepository,programRepository,facilityRepository,supervisoryNodeRepository);
  }

  @Test
  public void shouldReturnSupplyLineBySupervisoryNodeAndProgram() {
    Program program = new Program();
    SupervisoryNode supervisoryNode = new SupervisoryNode();
    SupplyLine supplyLine = new SupplyLine();
    when(supplyLineRepository.getSupplyLineBy(supervisoryNode, program)).thenReturn(supplyLine);

    SupplyLine returnedSupplyLine = supplyLineService.getSupplyLineBy(supervisoryNode, program);

    verify(supplyLineRepository).getSupplyLineBy(supervisoryNode, program);
    assertThat(returnedSupplyLine, is(supplyLine));
  }

  @Test
  public void shouldThrowErrorIfProgramDoesNotExist() {
    when(programRepository.getIdByCode(supplyLine.getProgram().getCode())).thenThrow(new DataException("Invalid program code"));

    expectedEx.expect(DataException.class);
    expectedEx.expectMessage("Invalid program code");
    supplyLineService.save(supplyLine);
  }

  @Test
  public void shouldThrowErrorIfFacilityDoesNotExist() {
    when(programRepository.getIdByCode(supplyLine.getProgram().getCode())).thenReturn(1);
    when(facilityRepository.getIdForCode(supplyLine.getSupplyingFacility().getCode())).thenThrow(new DataException("Invalid Facility Code"));

    expectedEx.expect(DataException.class);
    expectedEx.expectMessage("Invalid Facility Code");

    supplyLineService.save(supplyLine);
  }

  @Test
  public void shouldThrowErrorIfSupervisoryNodeDoesNotExist() {
    when(programRepository.getIdByCode(supplyLine.getProgram().getCode())).thenReturn(1);
    when(facilityRepository.getIdForCode(supplyLine.getSupplyingFacility().getCode())).thenReturn(1);
    when(supervisoryNodeRepository.getIdForCode(supplyLine.getSupervisoryNode().getCode())).thenThrow(new DataException("Invalid SupervisoryNode Code"));

    expectedEx.expect(DataException.class);
    expectedEx.expectMessage("Invalid SupervisoryNode Code");

    supplyLineService.save(supplyLine);
  }

  @Test
  public void shouldThrowErrorIfSupervisoryNodeIsNotTheParentNode() {
    when(programRepository.getIdByCode(supplyLine.getProgram().getCode())).thenReturn(1);
    when(facilityRepository.getIdForCode(supplyLine.getSupplyingFacility().getCode())).thenReturn(1);
    when(supervisoryNodeRepository.getIdForCode(supplyLine.getSupervisoryNode().getCode())).thenReturn(1);
    when(supervisoryNodeRepository.getSupervisoryNodeParentId(supplyLine.getSupervisoryNode().getId())).thenReturn(2);

    expectedEx.expect(DataException.class);
    expectedEx.expectMessage("Supervising Node is not the Top node");

    supplyLineService.save(supplyLine);
  }

  @Test
  public void shouldInsertSupplyLineIfDoesNotExist() throws Exception {
    when(programRepository.getIdByCode(supplyLine.getProgram().getCode())).thenReturn(1);
    when(facilityRepository.getIdForCode(supplyLine.getSupplyingFacility().getCode())).thenReturn(1);
    when(supervisoryNodeRepository.getIdForCode(supplyLine.getSupervisoryNode().getCode())).thenReturn(1);
    supplyLine.getSupervisoryNode().setId(1);
    when(supervisoryNodeRepository.getSupervisoryNodeParentId(1)).thenReturn(null);

    supplyLine.setId(null);

    supplyLineService.save(supplyLine);

    verify(supplyLineRepository).insert(supplyLine);
  }

  @Test
  public void shouldUpdateSupplyLineIfExists() throws Exception {
    when(programRepository.getIdByCode(supplyLine.getProgram().getCode())).thenReturn(1);
    when(facilityRepository.getIdForCode(supplyLine.getSupplyingFacility().getCode())).thenReturn(1);
    when(supervisoryNodeRepository.getIdForCode(supplyLine.getSupervisoryNode().getCode())).thenReturn(1);
    supplyLine.getSupervisoryNode().setId(1);
    when(supervisoryNodeRepository.getSupervisoryNodeParentId(1)).thenReturn(null);

    supplyLine.setId(1);

    supplyLineService.save(supplyLine);

    verify(supplyLineRepository).update(supplyLine);

  }

  @Test
  public void shouldGetExistingSupplyLine() throws Exception {
    when(programRepository.getIdByCode(supplyLine.getProgram().getCode())).thenReturn(1);
    when(facilityRepository.getIdForCode(supplyLine.getSupplyingFacility().getCode())).thenReturn(1);
    when(supervisoryNodeRepository.getIdForCode(supplyLine.getSupervisoryNode().getCode())).thenReturn(1);
    supplyLine.getSupervisoryNode().setId(1);
    when(supervisoryNodeRepository.getSupervisoryNodeParentId(1)).thenReturn(null);
    when(supplyLineRepository.getSupplyLineBySupervisoryNodeProgramAndFacility(supplyLine)).thenReturn(supplyLine);
    supplyLineService.save(supplyLine);

    SupplyLine result = supplyLineService.getExisting(supplyLine);

    assertThat(result, is(supplyLine));
  }
}
