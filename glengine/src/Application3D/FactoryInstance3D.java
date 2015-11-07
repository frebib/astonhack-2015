package Application3D;

import Application3D.Application3D;

public abstract class FactoryInstance3D implements Renderer3D {
    /**
     * A FactoryInstance3D is an instance which must be able to destroy itself.
     * FactoryInstances will only be created through the SCApplication3D factory
     * methods. This means the only remaining reference for the object will lie
     * within the registered instance list.
     * This means the object can deploy a "destruct" method which will remove it
     * from the list of registered instances.
     * 
     * This class is designed for temporary instances which are created throughout gameplay.
     * For example popup's
     * 
     * 
     */
    
    public FactoryInstance3D(){
        Application3D.getApp().registerRenderInstance( this );
    }
    
    public void destruct(){
        Application3D.getApp().registerDestructibleInstance( this );
    }
    
    
    public abstract void deleteBegin();
}
