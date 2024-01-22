import pytest
import json
from pprint import *
from worker import _solve as solver
from worker import _parseData as parser
from Scheduler import scheduleThresholdSearch

from Exceptions import *

import worker as rabbitmq_consumer

@pytest.fixture
def load_data(request):
    def _load_data(id):
        data = ""
        with open(f"test/{id}.json", "r") as fp:
            data = json.load(fp)
        return data
    return _load_data

@pytest.fixture
def load_expected(request):
    def _load_expected(id):
        data = ""
        with open(f"test/{id}_expected.json", "r") as fp:
            data = json.load(fp)
        return data
    return _load_expected

#-----------------------------------------------------------------------------------
#---------------------------------- Parser TESTS -----------------------------------
#-----------------------------------------------------------------------------------

def test_parser_positive_instance(load_data):
    data = load_data(0)
    (activities, schedule) = parser(data)
    assert True

def test_parser_negative_instance_request_field_missing(load_data):
    data = load_data(3)
    with pytest.raises(DataMalformedExcpetion):
        parser(data)

def test_parser_negative_instance_activity_field_missing(load_data):
    data = load_data(4)
    with pytest.raises(DataMalformedExcpetion):
        parser(data)


#-----------------------------------------------------------------------------------
#---------------------------------- CSP TESTS --------------------------------------
#-----------------------------------------------------------------------------------



def test_algorithm_positive_instance(load_data,load_expected,capsys):
    id_value = 0
    data = load_data(id_value)
    expected = load_expected(id_value)

    (activities, schedule) = parser(data)
    (threshold,resp) = scheduleThresholdSearch(activities, schedule, 1)
    resp = resp[0]
    print(resp.toJson())
    assert resp.getActivitiesAsJson() == expected["activities"]

def test_algorithm_negative_instance_intensity_constraint(load_data,load_expected,capsys):
    id_value = 1
    data = load_data(id_value)

    (activities, schedule) = parser(data)
    with pytest.raises(IntensityConstraintException):
        scheduleThresholdSearch(activities, schedule, 1)

def test_algorithm_negative_instance_trainer_table_constraint(load_data,load_expected,capsys):
    id_value = 2
    data = load_data(id_value)

    (activities, schedule) = parser(data)
    with pytest.raises(TrainerConstraintExcpetion):
        scheduleThresholdSearch(activities, schedule, 1)
