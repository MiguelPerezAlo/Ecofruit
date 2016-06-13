'use strict';

describe('Controller Tests', function() {

    describe('Categoria Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockCategoria, MockSubcategoria;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockCategoria = jasmine.createSpy('MockCategoria');
            MockSubcategoria = jasmine.createSpy('MockSubcategoria');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Categoria': MockCategoria,
                'Subcategoria': MockSubcategoria
            };
            createController = function() {
                $injector.get('$controller')("CategoriaDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'ecofruitApp:categoriaUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
