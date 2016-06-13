(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('SubcategoriaDeleteController',SubcategoriaDeleteController);

    SubcategoriaDeleteController.$inject = ['$uibModalInstance', 'entity', 'Subcategoria'];

    function SubcategoriaDeleteController($uibModalInstance, entity, Subcategoria) {
        var vm = this;

        vm.subcategoria = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Subcategoria.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
