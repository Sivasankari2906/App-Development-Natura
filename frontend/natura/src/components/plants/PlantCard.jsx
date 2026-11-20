import React from 'react';
import '../../styles/plant-card.css';

const PlantCard = ({ plant, onEdit, onDelete, onEnvironmentData, onHealthRecord, onCareTasks }) => {
  // Plant images based on plant type or use specific plant images
  const getPlantImage = () => {
    if (plant.imageUrl) return plant.imageUrl;
    
    // Use different plant images based on plant ID
    const plantImages = [
      'https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=300&h=200&fit=crop&q=80', // Monstera deliciosa
      'https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=300&h=200&fit=crop&q=80', // Succulent collection
      'https://admin.thegreenyard.in/uploads/product_images/524824870-rose-red-plant.jpg', // Rose plant
      'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcThyUMp0XCMKi_vIoakaPQRm3HhAVngMTKCWg&s', // Snake plant
      'https://i0.wp.com/cdn-prod.medicalnewstoday.com/content/images/articles/266/266425/basil-in-a-bowel-on-a-table.jpg?w=1155&h=1297', // Basil plant
      'https://www.ugaoo.com/cdn/shop/products/kesar-mango-plant-32113851662468.jpg?v=1673871657', // Mango sapling
      'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT6OGQ-btYWUaIOZaYjfApkcb5h1VOZxigfTw&s', // Tulip pot
      'https://images.unsplash.com/photo-1520412099551-62b6bafeb5bb?w=300&h=200&fit=crop&q=80', // Aloe vera
      'https://images.unsplash.com/photo-1501004318641-b39e6451bec6?w=300&h=200&fit=crop&q=80', // Barrel cactus
      'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=300&h=200&fit=crop&q=80', // Boston fern
      'https://images.unsplash.com/photo-1512428813834-c702c7702b78?w=300&h=200&fit=crop&q=80', // Spider plant
      'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop&q=80', // ZZ plant
      'https://images.unsplash.com/photo-1493663284031-b7e3aaa4cab7?w=300&h=200&fit=crop&q=80', // Philodendron
      'https://images.unsplash.com/photo-1502780402662-acc01917e4e6?w=300&h=200&fit=crop&q=80', // Jade plant
      'https://images.unsplash.com/photo-1518531933037-91b2f5f229cc?w=300&h=200&fit=crop&q=80', // Dracaena
      'https://images.unsplash.com/photo-1567225557594-88d73e55f2cb?w=300&h=200&fit=crop&q=80', // Calathea
      'https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=300&h=200&fit=crop&q=80', // Bamboo palm
      'https://images.unsplash.com/photo-1565011523534-747a8601f10a?w=300&h=200&fit=crop&q=80', // White orchid
      'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=300&h=200&fit=crop&q=80', // Tropical plants
      'https://images.unsplash.com/photo-1448375240586-882707db888b?w=300&h=200&fit=crop&q=80'  // Houseplant collection
    ];
    
    return plantImages[plant.id % plantImages.length];
  };
  
  const plantImage = getPlantImage();

  return (
    <div className="plant-card">
      {/* Display plant image */}
      <img
        src={plantImage}
        alt={plant.nickname}
        className="plant-img"
      />
      <div className="plant-details">
        {/* Displaying the plant's name */}
        <h3>{plant.nickname}</h3>
        
        {/* Displaying status, location, and notes */}
        <p><strong>Status:</strong> {plant.healthStatus}</p>
        <p><strong>Location:</strong> {plant.location}</p>
        <p><strong>Notes:</strong> {plant.notes}</p>

        {/* Action buttons */}
        <div className="plant-actions">
          <button className="edit-btn" onClick={onEdit}>Edit</button>
          <button className="delete-btn" onClick={onDelete}>Delete</button>
          <button className="care-btn" onClick={onCareTasks}>Care Tasks</button>
          <button className="env-btn" onClick={onEnvironmentData}>Environment Data</button>
          <button className="health-btn" onClick={onHealthRecord}>Health Record</button>
        </div>
      </div>
    </div>
  );
};

export default PlantCard;
