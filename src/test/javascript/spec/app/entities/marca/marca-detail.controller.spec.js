'use strict';

describe('Controller Tests', function() {

    describe('Marca Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockMarca, MockProducto;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockMarca = jasmine.createSpy('MockMarca');
            MockProducto = jasmine.createSpy('MockProducto');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Marca': MockMarca,
                'Producto': MockProducto
            };
            createController = function() {
                $injector.get('$controller')("MarcaDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'ecofruitApp:marcaUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
