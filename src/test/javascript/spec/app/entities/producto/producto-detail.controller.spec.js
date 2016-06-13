'use strict';

describe('Controller Tests', function() {

    describe('Producto Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockProducto, MockMarca, MockSubcategoria;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockProducto = jasmine.createSpy('MockProducto');
            MockMarca = jasmine.createSpy('MockMarca');
            MockSubcategoria = jasmine.createSpy('MockSubcategoria');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Producto': MockProducto,
                'Marca': MockMarca,
                'Subcategoria': MockSubcategoria
            };
            createController = function() {
                $injector.get('$controller')("ProductoDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'ecofruitApp:productoUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
